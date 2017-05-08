(ns tasks.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce http (nodejs/require "http"))

(def app (express))

(.get app "/"
      (fn [req res] (.send res "Hello, world")))

(defn -main []
  (let [port (some-> (.-env js/process)
                     (aget "PORT")
                     (js/parseInt))]
    (doto (.createServer http #(app %1 %2))
      (.listen (or port 3000)))))

(set! *main-cli-fn* -main)
