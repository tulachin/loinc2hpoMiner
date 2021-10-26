package org.monarchinitiative.loinc2hpo.io.loincparser;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class LoincVsHpoQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoincVsHpoQuery.class);

    private final Ontology hpo;
    /** Do not try to match on these words in LOINC labels. */
    private final static Set<String> omitWords = Set.of("in", "or", "and", "Serum", "Plasma", "Dose", "Blood");


    public static final String modifier = "increase.*|decrease.*|elevat.*|reduc.*|high.*|low.*|above|below|abnormal.*";

    public LoincVsHpoQuery(Ontology ontology) {
        hpo = ontology;
    }




    public List<HpoClassFound> queryByString(String keysString,
                         LoincLongNameComponents loincLongNameComponents) {
        String [] keys = keysString.split("[ ,-;]");
        return queryByString(List.of(keys), loincLongNameComponents);
    }

    public List<HpoClassFound> queryByString(List<String> queries,
                                             LoincLongNameComponents loincLongNameComponents) {
        return query_impl(queries, loincLongNameComponents);
    }


    public List<HpoClassFound> queryByLoincId(String keysString,
                                              LoincLongNameComponents loincLongNameComponents) {
        String [] keys = keysString.split(" ");
        return queryByLoincId(List.of(keys), loincLongNameComponents);
    }


    /**
     * A method to do manual query with provided keys (literally)
     */
    public List<HpoClassFound> queryByLoincId(List<String> keys,
                                              LoincLongNameComponents loincLongNameComponents) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            List<String> searchItems = new ArrayList<>();
            searchItems.add(loincLongNameComponents.getLoincTissue());
            searchItems.add(loincLongNameComponents.getLoincMethod());
            searchItems.add(loincLongNameComponents.getLoincParameter());
            searchItems.add(loincLongNameComponents.getLoincType());
            return query_impl(searchItems, loincLongNameComponents);
        }
    }


    private List<HpoClassFound> query_impl(List<String> items, LoincLongNameComponents loincLongNameComponents) {
        items = items.stream().filter(s -> ! omitWords.contains(s)).collect(Collectors.toList());
        LOGGER.info("got {} search items",items.size());
        List<HpoClassFound> foundList = new ArrayList<>();
        for (Term term : hpo.getTermMap().values()) {
            Set<String> words = getWords(term);
            for (var word : items) {
                if (words.contains(word)) {
                    foundList.add(new HpoClassFound(term, loincLongNameComponents));
                    break;
                }
            }
        }
        LOGGER.info("got {} found items",foundList.size());
        return foundList;
    }




    private Set<String> getWords(Term term) {
        Set<String> words = new HashSet<>();
        String label = term.getName();
        String [] labelWords = label.split("\s+");
        words.addAll(List.of(labelWords));
        for (var synonym: term.getSynonyms()) {
            String synonymLabel = synonym.getValue();
            //System.out.println(synonymLabel);
            String [] synonymWords = synonymLabel.split("\s+");
            words.addAll(List.of(synonymWords));
        }
        return words;
    }


}
