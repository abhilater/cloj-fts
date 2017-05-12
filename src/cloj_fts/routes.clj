(ns cloj-fts.routes
  (:require [compojure.core :refer [GET POST defroutes context]]
            [compojure.route :as route]
            [cloj-fts.core :as service]))

(defroutes routes
           (context "/documents" []
                    (GET "/" [] {:body (service/test-server)})
                    (POST "/" req (let [text (get (:params req) :text)
                                        id (get (:params req) :id)]
                                    {:status 202 :body {"created" (service/index-doc (service/create-doc text id))}}))
                    (context "/search" []
                             (GET "/" req (let [query (get (:params req) :query)]
                                            {:body (let [results (service/search query) count (count results)]
                                                     {"count" count "documents" results})}
                                            ))))
           (route/not-found "Not Found"))

(comment
  ;;curl -X POST --data "text=We are going to create a HUGE Wall&id=1" http://localhost:3000/documents
  ;;http://localhost:3000/documents/search?query=YES
  )
