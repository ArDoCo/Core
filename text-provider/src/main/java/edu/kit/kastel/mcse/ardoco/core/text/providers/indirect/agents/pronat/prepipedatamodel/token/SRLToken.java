/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Token describing an action recognized by the semantic role labeler including Information about the
 * corresponding role Tokens
 *
 * @author Sebastian Weigelt
 * @author Tobias Hey
 *
 */
public class SRLToken extends Token {

    private final HashMap<String, ArrayList<Token>> dependentTokens;

    private final HashMap<String, String[]> roleDescription;

    private String correspondingVerb;
    private Double roleConfidence;
    private String pbRolesetID;
    private String pbRolesetDescr;
    private String[] vnFrames;
    private String[] fnFrames;
    private String[] eventTypes;

    public SRLToken(Token feed) {
        super(feed);
        setPos(feed.getPos());
        setChunkIOB(feed.getChunkIOB());
        setChunk(feed.getChunk());
        setInstructionNumber(feed.getInstructionNumber());
        setNer(feed.getNer());
        setLemma(feed.getLemma());
        setStem(feed.getStem());

        dependentTokens = new HashMap<>();
        roleDescription = new HashMap<>();
    }

    /**
     * Add a {@link Token} as dependent of this {@link SRLToken} with the specified role
     *
     * @param role  the semantic role
     * @param token the token
     */
    public void addDependentToken(String role, Token token) {
        if (dependentTokens.containsKey(role)) {
            ArrayList<Token> tokens = dependentTokens.get(role);
            tokens.add(token);
            tokens.sort(Comparator.comparingInt(AbstractHypothesisToken::getPosition));

        } else {
            ArrayList<Token> tokens = new ArrayList<>();
            tokens.add(token);
            dependentTokens.put(role, tokens);
        }
    }

    /**
     * Add Information to a dependent Role of this {@link SRLToken} from PropBank, VerbNet and FrameNet
     *
     * @param role    the semantic role
     * @param pbRole  the propBank role
     * @param vnRoles the VerbNet role
     * @param fnRoles the FrameNet role
     */
    public void addRoleDescription(String role, String pbRole, String[] vnRoles, String[] fnRoles) {
        if (roleDescription.containsKey(role)) {
            String[] roleDescriptions = roleDescription.get(role);
            roleDescriptions[0] = pbRole;
            roleDescriptions[1] = Arrays.toString(vnRoles);
            roleDescriptions[2] = Arrays.toString(fnRoles);

        } else {
            var roleDescriptions = new String[3];
            roleDescriptions[0] = pbRole;
            roleDescriptions[1] = Arrays.toString(vnRoles);
            roleDescriptions[2] = Arrays.toString(fnRoles);
            roleDescription.put(role, roleDescriptions);
        }
    }

    /**
     * Get the Information of the specified Role from PropBank, VerbNet and FrameNet
     *
     * @param role the semantic role
     * @return the information for the role as provided by PropBank, VerbNet and FrameNet
     */
    public String[] getRoleDescriptions(String role) {
        if (!roleDescription.isEmpty() && roleDescription.containsKey(role)) {
            return roleDescription.get(role);
        }
        return new String[] {};
    }

    /**
     * Return all {@link Token} belonging to the Verb represented by this {@link SRLToken}
     *
     * @return the VerbNet tokens
     */
    public List<Token> getVerbTokens() {
        if (!dependentTokens.isEmpty() && dependentTokens.containsKey("V")) {
            return dependentTokens.get("V");
        }
        return new ArrayList<>();
    }

    /**
     * Return all {@link Token} belonging to the specified Role
     *
     * @param role the semantic role
     * @return the tokens that belong to the role
     */
    public List<Token> getTokensOfRole(String role) {
        if (!dependentTokens.isEmpty() && dependentTokens.containsKey(role)) {
            return dependentTokens.get(role);
        }
        return new ArrayList<>();
    }

    /**
     * Returns all dependent Roles of this {@link SRLToken}
     *
     * @return all dependent Roles of the {@link SRLToken}
     */
    public Set<String> getDependentRoles() {
        return dependentTokens.keySet();
    }

    /**
     * @return the correspondingVerb
     */
    public String getCorrespondingVerb() {
        return correspondingVerb;
    }

    /**
     * @param correspondingVerb the correspondingVerb to set
     */
    public void setCorrespondingVerb(String correspondingVerb) {
        this.correspondingVerb = correspondingVerb;
    }

    /**
     * @return the roleConfidence
     */
    public Double getRoleConfidence() {
        return roleConfidence;
    }

    /**
     * @param roleConfidence the roleConfidence to set
     */
    public void setRoleConfidence(Double roleConfidence) {
        this.roleConfidence = roleConfidence;
    }

    /**
     * @return the pbRolesetID
     */
    public String getPbRolesetID() {
        return pbRolesetID;
    }

    /**
     * @param pbRolesetID the pbRolesetID to set
     */
    public void setPbRolesetID(String pbRolesetID) {
        this.pbRolesetID = pbRolesetID;
    }

    /**
     * @return the pbRolesetDescr
     */
    public String getPbRolesetDescr() {
        return pbRolesetDescr;
    }

    /**
     * @param pbRolesetDescr the pbRolesetDescr to set
     */
    public void setPbRolesetDescr(String pbRolesetDescr) {
        this.pbRolesetDescr = pbRolesetDescr;
    }

    /**
     * @return the vnFrames
     */
    public String[] getVnFrames() {
        return vnFrames;
    }

    /**
     * @param vnFrames the vnFrames to set
     */
    public void setVnFrames(String[] vnFrames) {
        this.vnFrames = vnFrames;
    }

    /**
     * @return the fnFrames
     */
    public String[] getFnFrames() {
        return fnFrames;
    }

    /**
     * @param fnFrames the fnFrames to set
     */
    public void setFnFrames(String[] fnFrames) {
        this.fnFrames = fnFrames;
    }

    /**
     * @return the eventTypes
     */
    public String[] getEventTypes() {
        return eventTypes;
    }

    /**
     * @param eventTypes the eventTypes to set
     */
    public void setEventTypes(String[] eventTypes) {
        this.eventTypes = eventTypes;
    }

    @Override
    public int hashCode() {

        if (hash != 0) {
            return hash;
        }
        hash = super.hashCode();
        hash = correspondingVerb == null ? hash : 31 * hash + correspondingVerb.hashCode();
        hash = roleConfidence == null ? hash : 31 * hash + roleConfidence.hashCode();
        hash = pbRolesetID == null ? hash : 31 * hash + pbRolesetID.hashCode();
        hash = pbRolesetDescr == null ? hash : 31 * hash + pbRolesetDescr.hashCode();
        hash = vnFrames == null ? hash : 31 * hash + Arrays.hashCode(vnFrames);
        hash = fnFrames == null ? hash : 31 * hash + Arrays.hashCode(fnFrames);
        hash = eventTypes == null ? hash : 31 * hash + Arrays.hashCode(eventTypes);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj != null && obj.getClass() == this.getClass()) {
            SRLToken other = (SRLToken) obj;
            return super.equals(obj) && Objects.equals(correspondingVerb, other.getCorrespondingVerb()) && roleConfidence == other.getRoleConfidence()
                    && Objects.equals(pbRolesetID, other.getPbRolesetID()) && Objects.equals(getPbRolesetDescr(), other.getPbRolesetDescr())
                    && Arrays.deepEquals(vnFrames, other.getVnFrames()) && Arrays.deepEquals(fnFrames, other.getFnFrames())
                    && Arrays.deepEquals(eventTypes, other.getEventTypes());
        }
        return false;
    }
}
