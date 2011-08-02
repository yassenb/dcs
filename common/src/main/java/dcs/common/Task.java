package dcs.common;

import java.io.Serializable;

public interface Task<T extends Serializable> extends Serializable {
    public T execute();
}