(ns tasks.routes.tasks
  (:require [cljs.nodejs :as nodejs]
            [tasks.models.tasks-list :refer [TasksList]]))

(defn tasksRoute [req res]
  (-> TasksList
    (.find)
    (.sort "name")
    (.select "_id user name tasks")
    (.exec)
    (.then (fn [result]
             (.json res (clj->js
                         {:link "/tasks"
                          :tasksLists result}))))
    (.catch (fn [error]
              (-> res
                (.status 400)
                (.json error))))))

(defn router [app]
  (doto app
    (.get "/tasks" tasksRoute)))
