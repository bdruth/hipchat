# HipChat

A Clojure library for the HipChat API V2

## Usage

Get an auth token from HipChat.

```clojure

(ns your-ns
  (require [hipchat.core :as hc]))

(def auth-token "YOURAPITOKEN")

```

## Rooms

List all rooms

```clojure

(rooms auth-token)

;; => [{:id 325478, :links {:self "https://api.hipchat.com/v2/room/325478"}, :name "forward"}]

```

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
