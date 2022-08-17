public interface WebAPI<T1, T2> {
    public T1 sendApiRequest(String endpoint);
    public T1 sendApiRequest(String endpoint, T2 requestData);
}
