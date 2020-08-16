package editor;

public class IOResult<T> {
    private final T data;
    private final boolean ok;

    public IOResult(T data, boolean ok) {
        this.data = data;
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    public boolean hasData() {
        return data != null;
    }

    public T getData() {
        return data;
    }


}
