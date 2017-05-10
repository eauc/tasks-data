(ns tasks.routes.tasks
  (:require [cljs.nodejs :as nodejs]
            [tasks.models.tasks-list :refer [TasksList]]))

(defn- catchErrorResponse [result res]
  (.catch result
          #(-> res (.status 400) (.json %))))

(defn- mapId [x]
  (-> x
      (assoc :id (:_id x))
      (dissoc :_id)))

(defn- queryResult->tasksList [result]
  (-> (.toObject result)
      (js->clj :keywordize-keys true)
      (select-keys [:_id :name :tasks :updatedAt :user])
      (update :tasks #(mapv mapId %))
      (mapId)))

(defn- tasksListResponse [res result {:keys [status]}]
  (let [tasks-list (queryResult->tasksList result)]
    (-> res
        (.status (or status 200))
        (.json (clj->js
                 {:link (str "/tasks/" (:id tasks-list))
                  :tasksList tasks-list})))))

(defn tasksRoute [req res]
  (-> TasksList
    (.find)
    (.sort "name")
    (.select "_id user name")
    (.exec)
    (.then #(.json res (clj->js {:link "/tasks" :tasksLists %})))
    (catchErrorResponse res)))

(defn tasksCreateRoute [req res]
  (-> req
      (aget "body")
      (TasksList)
      (.save)
      (.then #(tasksListResponse res % {:status 201}))
      (catchErrorResponse res)))

(defn tasksByIdRoute [req res]
  (-> (.findById TasksList (aget req "params" "id"))
      (.exec)
      (.then
        #(if %
          (tasksListResponse res % {})
          (-> res (.status 404) (.json #js {:message "not found"}))))
      (catchErrorResponse res)))

(defn- updateTasksList [result params]
  (.save (doto result
           (aset "name" (or (aget params "name")
                            (aget result "name")))
           (aset "tasks" (or (aget params "tasks")
                             (aget result "tasks"))))))

(defn tasksUpdateRoute [req res]
  (-> (.findById TasksList (aget req "params" "id"))
      (.exec)
      (.then #(updateTasksList % (aget req "body")))
      (.then #(tasksListResponse res % {}))
      (catchErrorResponse res)))

(defn tasksRemoveRoute [req res]
  (-> (.remove TasksList #js {:_id (aget req "params" "id")})
      (.then #(-> res (.status 200) (.body %)))
      (catchErrorResponse res)))

(defn router [app]
  (doto app
    (.get "/tasks" tasksRoute)
    (.post "/tasks" tasksCreateRoute)
    (.get "/tasks/:id" tasksByIdRoute)
    (.put "/tasks/:id" tasksUpdateRoute)
    (.delete "/tasks/:id" tasksRemoveRoute)))
