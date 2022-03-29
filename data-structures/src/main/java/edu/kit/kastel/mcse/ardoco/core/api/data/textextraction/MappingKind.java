/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

/**
 * The mapping type of a mapping states whether the mapping is a name or a type.
 *
 * @author Sophie
 *
 */
public enum MappingKind {

    /**
     * A noun mapping can be identified as an identifier {@link #NAME}, a potential type {@link #TYPE}, or a noun that
     * can not be clearly sorted to one of them {@link #NAME_OR_TYPE}
     */
    NAME,
    /**
     * A noun mapping can be identified as an identifier {@link #NAME}, a potential type {@link #TYPE}, or a noun that
     * can not be clearly sorted to one of them {@link #NAME_OR_TYPE}
     */
    TYPE

}
