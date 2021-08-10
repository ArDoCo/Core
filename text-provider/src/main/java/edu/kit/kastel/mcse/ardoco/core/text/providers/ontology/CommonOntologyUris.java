package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

enum CommonOntologyUris {

    TEXT_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_2abdbab4_07a1_44e4_86b4_1dd2db60d093"),
    POS_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_4a42b4d3_f585_4d3a_ac8d_12efcd2f41ed"),
    LEMMA_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_8641228e_89c1_4094_8770_d6db4cff934d"),
    POSITION_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_24a1fab1_8d82_4a64_a569_26181935ae92"),
    SENTENCE_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_cca1bf08_930a_4c38_b1b4_4b127db235a3"),
    HAS_ITEM_PROPERTY("https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#item"),
    HAS_NEXT_PROPERTY("https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#next"),
    HAS_PREVIOUS_PROPERTY("https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#previous"),
    DEP_SOURCE_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_338dfb91_e78b_4145_bf8c_a952e927b6e9"),
    DEP_TARGET_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_82e64c17_5998_4d50_941f_a2b859c1a95b"),
    DEP_TYPE_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLAnnotationProperty_79e191d9_7e85_461e_ae42_62df5078719b"),
    HAS_WORDS_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_8acb94df_fd3f_4535_bd65_f43bc50f7c8d"),
    UUID_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base.owl#OWLDataProperty_d45dcc42_a463_476d_a06f_939637c6bc1c"),
    TEXT_DOCUMENT_CLASS("https://informalin.github.io/knowledgebases/informalin_base_text#OWLClass_f7ee71e0_fe7c_4640_b432_bb876416974a"),
    WORD_CLASS("https://informalin.github.io/knowledgebases/informalin_base#OWLClass_33cd62aa_e856_4bd3_93fd_454951b453b0"),
    WORD_DEPENDENCY_CLASS("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLClass_23b11e6d_0c9d_48a6_98c6_27ce9f2ceffb"),
    COREF_CLUSTER_CLASS("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLClass_b5f49fac_9778_48c1_9462_f6dd11ae711f"),
    COREF_MENTION_CLASS("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLClass_17dae5c7_2e82_4fef_a884_48d0c7c4561a"),
    HAS_MENTION_PROPERTY("https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_065d33e1_eb0a_4143_842a_dfdf5e49497a"),
    REPRESENTATIVE_MENTION_PROPERTY(
            "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_743f0626_55af_428c_a2ac_d04102108ff7");

    private String uri;

    CommonOntologyUris(String uri) {
        this.uri = uri;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

}
