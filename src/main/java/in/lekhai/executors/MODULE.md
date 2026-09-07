# executors

The **background-task bridge** that keeps firm isolation intact when work runs off the request thread. In Tally terms this is the difference between doing a job "at the desk" and having the office run it in the background — except here, the background job must still know *whose* books it is touching.

## Business Goal

Some work can't (or shouldn't) run inline on a request — scheduled EWB maintenance, report exports, long batch imports. Those tasks run asynchronously, but an async thread alone does **not** inherit the calling request's firm context (`ShopContext` is thread-local). Without it, a background job loses its RLS scoping and either sees no rows or fails outright — or, worse, could act with the wrong shop's context. This module ensures every `@Async` task starts with the same firm context that triggered it, and that the context is cleaned up afterwards so pooled worker threads never leak one firm's context into another job.

## Public API

- `lekhai/ContextDecorator` — a `TaskDecorator` that captures the current `ShopContext` (shop code) before a task is handed to a worker thread and restores it inside that thread; it also clears the context when the task finishes to prevent cross-job leakage between pooled threads.

## Dependencies

- `shop/` (`ShopContext`).

## How to Extend

- Any new `@Async` path that reads/writes shop-scoped data must be registered on an executor decorated with `ContextDecorator`. Register additional task executors and decorate them the same way for shop-scoped background work.

## Deep Dive

> Read `DETAILS.md` only when: adding async executors or changing context propagation.