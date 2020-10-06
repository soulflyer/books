(defproject books "0.1.0-SNAPSHOT"
  :dependencies [[http-kit "2.5.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773" :exclusions [com.google.javascript/closure-compiler-unshaded
                                                                    org.clojure/google-closure-library
                                                                    org.clojure/google-closure-library-third-party]]
                 [pneumatic-tubes "0.3.0"]
                 [re-frame "1.1.1"]
                 [re-frisk "1.3.4"]
                 [reagent "0.10.0"]
                 [thheller/shadow-cljs "2.11.0"]
                 ;; DO NOT sort this list. monger must come last.
                 [com.novemberain/monger "3.5.0"]]

  :plugins [[lein-shadow "0.2.2"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.9.0"

  :jvm-opts ["-Xmx1G"]

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :shadow-cljs {:nrepl {:port 8777}

                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules {:app {:init-fn books.core/init
                                               :preloads [devtools.preload
                                                          re-frisk.preload]}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280}}}}

  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open"  {:windows         ["cmd" "/c" "start"]
                              :macosx          "open"
                              :linux           "xdg-open"}}}

  :aliases {"watch"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "app" "browser-test" "karma-test"]]

            "release"      ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "ci"           ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :repl-options {:init-ns dev.main
                 :nrepl-middleware [shadow.cljs.devtools.server.nrepl/middleware]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]]
    :source-paths ["dev"]}

   :prod {}
   }

  :prep-tasks [])
