(ns vega-plot-setup
  (:require [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]))


(def ptselect
  (-> {:mark :point
       :encoding :ENCODING
       :params [{:name :pname> :select :pselect>}]}
      (assoc :aerial.hanami.templates/defaults
             {:OPACITY {:condition {:param :pname> :empty :empty> :value 1}
                        :value 0}
              :ENCODING {:opacity :OPACITY}
              :pname> "pselect" :empty> false
              :pselect> {:type :point :encodings :encodings>
                         :nearest :nearest> :on :mouseover}
              :encodings> ["x"] :nearest> true})))


(def slope-rule
  {:mark (merge ht/mark-base {:type "rule"})
   :transform [{:filter {:param :pname>, :empty false}}]
   :encoding (assoc ht/xy-encoding
                    :y2 {:field :Y2 :type :Y2TYPE :bin :Y2BIN
                         :axis :Y2AXIS :scale :Y2SCALE :sort :Y2SORT
                         :aggregate :Y2AGG}
                    :x2 {:field :X2 :type :X2TYPE :bin :X2BIN
                         :axis :X2AXIS :scale :X2SCALE :sort :X2SORT
                         :aggregate :X2AGG})
   :aerial.hanami.templates/defaults
   {:MCOLOR "red"
    :XSCALE {:zero false} :YSCALE {:zero false}
    :X2SCALE {:zero false} :Y2SCALE {:zero false}
    :X2 :x2 :X2TYPE :quantitative :Y2 :y2 :Y2TYPE :quantitative
    :X2BIN hc/RMV :X2AXIS hc/RMV :X2SORT hc/RMV :X2AGG hc/RMV
    :Y2BIN hc/RMV :Y2AXIS hc/RMV :Y2SORT hc/RMV :Y2AGG hc/RMV}})
