package programming.set8.links;


public class LinkedElement<T> {

    private T item;
    private LinkedElement<T> next;

    public LinkedElement(T item) {
        this.item = item;
    }

    /**
     * Returns the value of the i-th linked element.
     *
     * @param i
     *            0-based index of the element whose value to return.
     * @return the i-th element's value, or {@code null} if there is no element with that index.
     */
    public T get(int i) {
        if (i == 0) {
            return this.item;
        } else if (this.next != null ){
            return this.next.get(i-1);
        } else {
            return null;
        }
    }

    /**
     * Sets the value of the i-th linked element to the given value. If there is no i-th linked
     * element, nothing happens.
     *
     * @param i
     *            0-based index of the element whose value to return.
     * @param newVal
     *            the new value to set.
     */
    public void set(int i, T newVal) {
        if (i == 0) {
            this.item = newVal;
        } else if (next != null) {
            this.next.set(i - 1, newVal);
        }
    }

    /**
     * Returns the index of the first occurrence of a linked element carrying the given value in
     * the list.
     *
     * @param val
     *            the value to search for.
     * @return index where the value was found, or -1 if it's not in any of the linked elements.
     */
    public int firstIndexOf(T val) {
        if (this.item.equals(val)) {
            return 0;
        }
        if (next != null) {
            int nextIndex = this.next.firstIndexOf(val);
            if (nextIndex != -1) {
                return 1 + this.next.firstIndexOf(val);
            }
        }
        return -1;
    }

    /**
     * Adds a new linked element holding the given value at the end of the linked elements.
     *
     * @param newVal
     *            the new value.
     */
    public void add(T newVal) {
        if (this.next == null) {
            this.next = new LinkedElement<>(newVal);
        } else {
            this.next.add(newVal);
        }
    }

    /**
     * Removes the i-th element from the linked elements. If {@code i == 0}, this will effectively
     * remove the head element. Thus, this method returns the linked element that is the new head
     * element.
     *
     * @param i
     *            index of the element to remove.
     * @return the new head element.
     */
    public LinkedElement<T> remove(int i) {
        if (i == 0) {
            return this.next;
        } else if (this.next != null){
            this.next = this.next.remove(i - 1);
        }
        return this;
    }
}
