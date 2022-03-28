#!/bin/bash
# Author: Dominik Fuchss
# Show query here: https://dbpedia.org/sparql/

# Query for ComputerScienceWordsAgent
# SELECT DISTINCT ?alabel
# WHERE {
#   ?a rdfs:label ?alabel .
#   ?a dct:subject ?sc .
#   ?sc skos:broader{0,4} <http://dbpedia.org/resource/Category:Software_engineering> .
#   optional {?sc rdfs:label ?label  } .
#   ?sc ?p ?o
#   filter (lang(?label) = "en")
#   filter ((lang(?o) = "en") || isURI(?o))
#   filter (lang(?alabel) = "en")
# }
wget -O common-cs.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+DISTINCT+%3Falabel%0D%0AWHERE+{%0D%0A++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A++%3Fsc+skos%3Abroader{0%2C4}+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A++optional+{%3Fsc+rdfs%3Alabel+%3Flabel++}+.%0D%0A++%3Fsc+%3Fp+%3Fo%0D%0A++filter+(lang(%3Flabel)+%3D+%22en%22)%0D%0A++filter+((lang(%3Fo)+%3D+%22en%22)+||+isURI(%3Fo))%0D%0Afilter+(lang(%3Falabel)+%3D+%22en%22)%0D%0A}&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"