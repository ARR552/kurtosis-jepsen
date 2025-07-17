(defproject jepsen-file-exists "0.1.0-SNAPSHOT"
  :description "Minimal Jepsen test for file creation and validation"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [jepsen "0.2.5"]] ;; Use a recent version from https://github.com/jepsen-io/jepsen/tags
  :main jepsen.create-file
  :jvm-opts ["-Xmx2g"]
  :repositories [["clojars" "https://repo.clojars.org/"]
                 ["sonatype" "https://oss.sonatype.org/content/repositories/releases/"]])