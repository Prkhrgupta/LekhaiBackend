# executors

Async task configuration, notably propagating `ShopContext` across thread boundaries.

## Public API

- `lekhai/ContextDecorator` — `TaskDecorator` that copies `ShopContext` into async tasks (`@Async`).

## Dependencies

- `shop/` (`ShopContext`)

## How to Extend

- Register additional task executors and decorate them with `ContextDecorator` for shop-scoped async work.

## Deep Dive

> Read `DETAILS.md` only when: adding async executors or changing context propagation.