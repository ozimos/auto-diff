(ns user
  (:require [nextjournal.clerk :as clerk]
            [clojure.string :as str]))

;; set :browse? to true to open a clerk browser tab automatically
(clerk/serve! {:browse false :port 7777 
               :watch-paths ["notebooks"]
               :show-filter-fn #(str/includes? % "auto_diff")})


(comment
  (clerk/show! "notebooks/auto_diff.clj")

  )
