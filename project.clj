(defproject imajes "0.1"
  :description "image management library (currently only in s3)"
  :url "https://github.com/pierrel/imajes"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-aws-s3 "0.3.3"]]
  :plugins [[lein-swank "1.4.4"]])
