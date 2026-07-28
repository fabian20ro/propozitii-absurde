import handler, {
  configureRuntime,
  type VercelRequestLike,
  type VercelResponseLike
} from "../shared-api/src/domain.js";

export * from "../shared-api/src/domain.js";

let configured = false;

export default async function vercelHandler(req: VercelRequestLike, res: VercelResponseLike) {
  if (!configured) {
    configureRuntime(process.env);
    configured = true;
  }
  return handler(req, res);
}
