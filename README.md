# HipChat

A Clojure library for the HipChat API V2

https://www.hipchat.com/docs/apiv2/auth

## Usage

Get an auth token from HipChat.

```clojure

(ns your-ns
  (require [hipchat.core :as hc]))

(hc/set-auth-token! "YOURHIPCHATAPITOKEN")

```

You can wrap all functions in a with-token macro if you want to pass your auth
token explicitly into a request

```clojure
(hc/with-token "YOURTOKEN"
  (hc/rooms))
```

## Rooms

List all rooms

```clojure
(hc/rooms))

;; => [{:id 325478, :links {:self "https://api.hipchat.com/v2/room/325478"}, :name "forward"}]

```

Create a new HipChat room

```clojure

(hc/create-room "Product ideas")

```

Send a message to a hipchat room

```clojure
(hc/create-message "startups" "Hi @user. How are you?")

;; An optional params hash can be passed in to set the color of the hipchat message
;; See the API docs for other params that can be passed in
(hc/create-message "startups" "Hi @user. How are you?" {:color "red"})
```

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
