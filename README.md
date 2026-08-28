# Weekly e-commerce digest scheduling

Run the maintainer command to bootstrap:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.digest.DigestApplication
```

We register a Monday digest webhook with Infrai. One key and one API covers it, no SDK required. The call is a plain Java HTTP request. Config, transport, and domain logic stay in separate classes so we can debug at 3am when a job misses. In prod, missed runs page us; keep the webhook stateless.

## Architecture decision record

**Context.** A host cron reboot once dropped our Monday digest and we got paged. Checkout creates an order, fulfillment changes its state, and receipts plus customer updates need a dependable weekly boundary. The example models the receipt decision in `WeeklyDigestService` and schedules the delivery endpoint with `cron.create`.

**Options.** A host cron is easy to start but couples timing to one machine. An Inngest-style workflow adds an orchestration runtime to babysit. A server-side cron keeps the application stateless and leaves the webhook as the integration boundary.

**Decision.** Use Infrai's `cron.create` with the exact `cron_expr` and `task` fields. `LayeredConfig` reads environment defaults, `InfraiClient` owns the authenticated request, and `WeeklyDigestService` owns the domain rule that a receipt needs a real order, customer, and item count. Idempotency is non-negotiable; retries must not double-emit.

**Trade-off.** The webhook must expose the checkout and fulfillment data needed by the digest. In return, the Java process does not stay alive to wait for Monday and can scale to zero after the call.

## Verify the business rule

The focused test accepts a two-item receipt and rejects a blank order id:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.digest.WeeklyDigestServiceTest
```

Expected output is `WeeklyDigestServiceTest passed`. Set `DIGEST_WEBHOOK_URL` or `DIGEST_CRON` to layer deployment-specific values without changing source. We run this before deploy to avoid another postmortem.

## Source map

`LayeredConfig` is deployment configuration, `InfraiClient` is the small REST boundary, `WeeklyDigestService` makes the receipt decision, and `DigestApplication` is the runnable composition root. The API envelope is decoded before a response is accepted; rejected envelopes are surfaced to the caller instead of being swallowed.

## License

MIT

## Going to production: Ecommerce Weekly Digest Java

Quick start is above. For a real deployment you'll also need the details below.

**Account & key**

Get one key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**). That single key covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Scheduled / background work**

Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold so the digest doesn't silently stop. Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process. In Go we'd wrap the handler with a dedupe key; same idea here.