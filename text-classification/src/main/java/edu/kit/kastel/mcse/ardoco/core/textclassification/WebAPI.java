/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textclassification;

/**
 * the WebAPI interface defines methods for sending and receiving data from a web API.
 * 
 * @param <T1> the type of the responses
 * @param <T2> the type of data to be sent
 */
public interface WebAPI<T1, T2> {
    /**
     * sends a request without data to a specific API endpoint and returns the corresponding response
     * 
     * @param endpoint the API endpoint to send the request to
     * @return the corresponding API response
     */
    T1 sendApiRequest(String endpoint);

    /**
     * send a request with data to a specific API endpoint and return the response
     * 
     * @param endpoint    the API endpoint to send the request to
     * @param requestData the data to be sent with the request
     * @return the corresponding API response
     */
    T1 sendApiRequest(String endpoint, T2 requestData);
}
