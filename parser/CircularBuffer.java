package parser;

import java.util.Arrays;

public class CircularBuffer<T> {
    private final int capacity;
    private final T[] buffer;
    private int beginIndex;
    private int size;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
        this.beginIndex = 0;
        this.size = 0;
    }

    public void add(T value) {
        if (size >= capacity) {
            throw new IndexOutOfBoundsException();
        }
        int newIndex = index(size);
        buffer[newIndex] = value;
        size++;
    }

    public T peek() {
        return peek(0);
    }

    public T peek(int position) {
        if (size <= position) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[index(position)];
    }

    public T remove() {
        if (size == 0) {
            throw new IndexOutOfBoundsException();
        }
        T removedValue = buffer[beginIndex];
        buffer[beginIndex] = null;
        beginIndex = index(1);
        size--;
        return removedValue;
    }

    public int getSize() {
        return size;
    }

    private int index(int i) {
        return (i + beginIndex) % capacity;
    }

    @Override
    public String toString() {
        return "CircularBuffer{" +
                "buffer=" + Arrays.toString(buffer) +
                ", startIndex=" + beginIndex +
                ", size=" + size +
                '}';
    }
}
