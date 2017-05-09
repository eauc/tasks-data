(ns ^:figwheel-always tasks.core
  (:require [cljs.nodejs :as nodejs]
            [tasks.db :as db]
            [tasks.debug :as debug]
            [tasks.libs :as libs]
            [tasks.routes.index :as index]
            [tasks.routes.tasks :as tasks]))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce http (nodejs/require "http"))

(defonce state (js-obj {}))

(defn create-app []
  (debug/spy "Create express app")
  (aset state "app"
        (-> (express)
            (libs/init)
            (index/router)
            (tasks/router))))

(defn -main []
  (let [port (some-> (.-env js/process)
                     (aget "PORT")
                     (js/parseInt))]
    (db/init)
    (create-app)
    (doto (.createServer http #((aget state "app") %1 %2))
      (.listen (debug/spy "App starting on port" (or port 3000))))))

(set! *main-cli-fn* -main)
