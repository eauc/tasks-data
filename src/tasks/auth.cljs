(ns tasks.auth
  (:require [cljs.nodejs :as nodejs]))

(defonce jwt (nodejs/require "express-jwt"))
(defonce jwt-permissions (nodejs/require "express-jwt-permissions"))

(def secret
  (aget js/process "env" "AUTH_SECRET"))

(def auth
  (jwt #js {:secret secret}))

(def perms
  (jwt-permissions))
