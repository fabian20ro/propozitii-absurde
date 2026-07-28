import handler, { configureRuntime, type VercelRequestLike, type VercelResponseLike } from "./domain.js";

export * from "./domain.js";

interface Dependencies {
  logger: {
    debug(message: string, fields?: Record<string, unknown>): void;
    info(message: string, fields?: Record<string, unknown>): void;
    warn(message: string, fields?: Record<string, unknown>): void;
    error(message: string, fields?: Record<string, unknown>): void;
  };
  clock: { now(): number };
  transport?: { fetch(request: Request): Promise<Response> };
}

function required(config: Readonly<Record<string, unknown>>, name: string): string {
  const value = config[name];
  if (typeof value !== "string" || !value.trim()) throw new Error(`Missing ${name}`);
  return value.trim();
}

let singleton:
  | {
      url: string;
      key: string;
      origins: string;
      timeout: number;
      transport: Dependencies["transport"];
      handler: (request: Request) => Promise<Response>;
    }
  | undefined;

export function createHandler(
  config: Readonly<Record<string, unknown>>,
  dependencies: Dependencies
): (request: Request) => Promise<Response> {
  const origins = config.ALLOWED_ORIGINS;
  if (!Array.isArray(origins) || origins.some((origin) => typeof origin !== "string")) {
    throw new Error("ALLOWED_ORIGINS must be a string array");
  }
  const url = required(config, "SUPABASE_URL");
  const key = required(config, "SUPABASE_PUBLISHABLE_KEY");
  const originKey = origins.join(",");
  const timeout = Number(config.GENERATOR_TIMEOUT_MS ?? 7000);
  if (singleton) {
    if (
      singleton.url === url &&
      singleton.key === key &&
      singleton.origins === originKey &&
      singleton.timeout === timeout &&
      singleton.transport === dependencies.transport
    ) {
      return singleton.handler;
    }
    throw new Error("Propozitii module is already configured in this isolate");
  }

  configureRuntime(
    {
      SUPABASE_URL: url,
      SUPABASE_PUBLISHABLE_KEY: key,
      ALLOWED_ORIGINS: originKey,
      GENERATOR_TIMEOUT_MS: String(timeout)
    },
    dependencies.transport
      ? ((input: RequestInfo | URL, init?: RequestInit) => dependencies.transport!.fetch(new Request(input, init)))
      : undefined
  );

  const webHandler = async (request: Request): Promise<Response> => {
    const url = new URL(request.url);
    if (url.pathname !== "/api/all") return new Response("Not found", { status: 404 });
    const query = Object.fromEntries(url.searchParams.entries());
    let status = 200;
    const headers = new Headers();
    let body: unknown;
    const responseLike: VercelResponseLike = {
      setHeader(name, value) { headers.set(name, value); },
      status(code) {
        status = code;
        return {
          end() { body = undefined; },
          json(value) { body = value; }
        };
      }
    };
    const requestLike: VercelRequestLike = {
      method: request.method,
      query,
      headers: Object.fromEntries(request.headers.entries())
    };
    await handler(requestLike, responseLike);
    if (body === undefined) return new Response(null, { status, headers });
    headers.set("Content-Type", "application/json; charset=utf-8");
    return new Response(JSON.stringify(body), { status, headers });
  };
  singleton = { url, key, origins: originKey, timeout, transport: dependencies.transport, handler: webHandler };
  return webHandler;
}
