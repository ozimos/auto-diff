(require
 '[clojure.java.browse :as browse]
 '[nextjournal.clerk.webserver :as webserver]
 '[nextjournal.clerk :as clerk]
 '[nextjournal.beholder :as beholder])

(def port 7777)

(webserver/start! {:port port})

;; (clerk/show! "notebooks/auto_diff.clj")

(comment
  ;; Optionally start a file-watcher to automatically refresh notebooks when saved
  (def filewatcher
    (beholder/watch #(clerk/file-event %) "notebooks"))

  (beholder/stop filewatcher)

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/auto_diff.clj")


  (browse/browse-url (str "http://localhost:" port))

  )
