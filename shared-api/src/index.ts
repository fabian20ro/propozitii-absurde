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

export function createHandler(
  config: Readonly<Record<string, unknown>>,
  dependencies: Dependencies
): (request: Request) => Promise<Response> {
  const origins = config.ALLOWED_ORIGINS;
  if (!Array.isArray(origins) || origins.some((origin) => typeof origin !== "string")) {
    throw new Error("ALLOWED_ORIGINS must be a string array");
  }
  configureRuntime(
    {
      SUPABASE_URL: required(config, "SUPABASE_URL"),
      SUPABASE_PUBLISHABLE_KEY: required(config, "SUPABASE_PUBLISHABLE_KEY"),
      ALLOWED_ORIGINS: origins.join(","),
      GENERATOR_TIMEOUT_MS: String(config.GENERATOR_TIMEOUT_MS ?? 7000)
    },
    dependencies.transport
      ? ((input: RequestInfo | URL, init?: RequestInit) => dependencies.transport!.fetch(new Request(input, init)))
      : undefined
  );

  return async (request: Request): Promise<Response> => {
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
}
