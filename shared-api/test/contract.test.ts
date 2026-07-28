import { describe, expect, it, vi } from "vitest";
import { addDexLinks, createHandler, decorateVerse } from "../src/index.js";

const logger = { debug: vi.fn(), info: vi.fn(), warn: vi.fn(), error: vi.fn() };

describe("Propoziții module contract", () => {
  it("preserves HTML and verse delimiter contracts", () => {
    expect(decorateVerse("unu / doi")).toContain("<br/>");
    const html = addDexLinks("Ana are mere.");
    expect(html).toContain('target="_blank"');
    expect(html).toContain('rel="noopener"');
    expect(html).toContain("data-word=");
  });

  it("rejects routes outside /api/all", async () => {
    const handle = createHandler(
      {
        SUPABASE_URL: "https://example.supabase.co",
        SUPABASE_PUBLISHABLE_KEY: "sb_publishable_test",
        ALLOWED_ORIGINS: ["https://fabian20ro.github.io"]
      },
      { logger, clock: { now: () => 0 }, transport: { fetch: vi.fn() } }
    );
    expect((await handle(new Request("https://host/api/other"))).status).toBe(404);
  });
});
