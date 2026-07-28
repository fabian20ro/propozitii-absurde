# Propoziții shared API module

Provider-neutral `/api/all` implementation pinned to
[shared-api-contract v2.0.0](https://github.com/fabian20ro/shared-api-contract/releases/tag/v2.0.0).

Supabase is an injected external dependency. Only a publishable key protected by
RLS is accepted; service-role fallback is absent from the module configuration.
The response, sanitizer attributes and literal `" / "` verse delimiter remain
contractual.
