package edu.kit.kastel.mcse.ardoco.core.textclassification;

public interface WebAPI<T1, T2> {
    T1 sendApiRequest(String endpoint);
    T1 sendApiRequest(String endpoint, T2 requestData);
}
