package queue;

/**
 * Basic bidirectional queue
 * @param <T> Data type to hold
 */
public class BasicQueue<T> {
    /** Queue head (first element) */
    protected QueueCell head;
    /** Queue tail (last element) */
    protected QueueCell tail;
    /** Current amount of elements contained within queue */
    private int currentSize;

    /**
     * Default constructor, explicitly sets all fields to their default values
     */
    public BasicQueue() {
        this.currentSize = 0;
    }

    /**
     * Obtain reference to the element at the front of this queue
     * @return Element from the current front
     */
    public T getHead() {
        return head.data;
    }

    /**
     * Obtain reference to the element at the back of this queue
     * @return Element from the current back
     */
    public T getTail() {
        return tail.data;
    }

    /**
     * Provides exact amount of currently contained items
     * @return Amount of items contained
     */
    public int size() {
        return currentSize;
    }

    /**
     * Add given element to the back of this queue
     * @param data Added element
     */
    public void enqueue(T data) {
        addBack(data);
    }

    /**
     * Remove element at the front of this queue
     * @return Element removed
     */
    public T dequeue() {
        if (head != null) {
            T data = head.getData();
            removeCell(head);
            return data;
        }
        return null;
    }

    /**
     * Removes element pointed to by given iterator, if it is part of this queue
     * @param where Iterator to element for removal
     * @return True if element has been found and removed, false otherwise
     */
    public boolean removeAt(Iterator where) {
        if (where != null && where.isValid()) {
            removeCell(where.currentCell);
            return true;
        }
        return false;
    }

    /**
     * Add given element at the front of this queue
     * @param data Element to be added
     */
    protected void addFront(T data) {
        QueueCell cell = new QueueCell(data, head, null);
        if (head != null)
            head.setPrev(cell);
        head = cell;
        if (tail == null)
            tail = cell;
        ++currentSize;
    }

    /**
     * Add given element at the back of this queue
     * @param data Element to be added
     */
    protected void addBack(T data) {
        QueueCell newCell = new QueueCell(data, null, tail);
        if (tail != null)
            tail.setNext(newCell);
        tail = newCell;
        if (head == null)
            head = newCell;
        ++currentSize;
    }

    /**
     * Remove given cell from the queue, assuming it's currently a valid part of this queue object. In order to use it, you need to be absolutely sure the cell is inside this queue.
     * @param cell Cell to be removed - providing cell from another queue can cause damage!
     */
    protected void removeCell(QueueCell cell) {
        if (cell != null) {
            if (head == cell)
                head = cell.getNext();
            if (tail == cell)
                tail = cell.getPrev();
            if (cell.getNext() != null)
                cell.getNext().setPrev(cell.getPrev());
            if (cell.getPrev() != null)
                cell.getPrev().setNext(cell.getNext());
            cell.setNext(null);
            cell.setPrev(null);
            --currentSize;
        }
    }

    /**
     * Removes first encountered reference to given element, if found
     * @param data Reference to removed element
     * @return True if given element has been found, false otherwise
     */
    protected boolean removeFirstOccurrence(T data) {
        boolean hasElementBeenRemoved = false;
        if (head != null) {
            QueueCell currentlyViewedCell = head;
            while (currentlyViewedCell != null && currentlyViewedCell.getData() != data) {
                currentlyViewedCell = currentlyViewedCell.getNext();
            }
            if (currentlyViewedCell != null) {
                hasElementBeenRemoved = true;
                removeCell(currentlyViewedCell);
                --currentSize;
            }
        }
        return hasElementBeenRemoved;
    }

    /**
     * Get simple iterator for this queue
     * @return Iterator pointing to first element of the queue; could be invalid if queue is empty
     */
    public Iterator getIterator() {
        return new Iterator(this, this.head);
    }

    protected class QueueCell {

        private T data;
        private QueueCell next;
        private QueueCell prev;

        QueueCell(T data) {
            this.data = data;
        }

        QueueCell(QueueCell next, QueueCell prev) {
            this.next = next;
            this.prev = prev;
        }

        QueueCell(T data, QueueCell next, QueueCell prev) {
            this.next = next;
            this.prev = prev;
            this.data = data;
        }

        public QueueCell getNext() {
            return this.next;
        }

        public QueueCell getPrev() {
            return this.prev;
        }

        public T getData() {
            return this.data;
        }

        public void setNext(QueueCell next) {
            this.next = next;
        }

        public void setPrev(QueueCell prev) {
            this.prev = prev;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    /**
     * Simple bidirectional iterator. Does not provide invalidation checking other than basic null cell check, thus make sure to discard it after modifying queue.
     */
    public class Iterator {
        protected final BasicQueue<T> owner;
        protected QueueCell currentCell;

        Iterator(BasicQueue<T> owner, QueueCell currentCell) {
            this.owner = owner;
            this.currentCell = currentCell;
        }

        public T getData() {
            return (currentCell != null) ? currentCell.getData() : null;
        }

        public boolean isValid() {
            if (currentCell != null) {
                boolean valid = true;
                if (getNext() != null) {
                    valid = getNext().getPrev().currentCell == currentCell;
                }
                else {
                    valid = owner.tail == currentCell;
                }
                if (getPrev() != null) {
                    valid = getPrev().getNext().currentCell == currentCell && valid;
                }
                else {
                    valid = owner.head == currentCell && valid;
                }
                return valid;
            }
            return false;
        }

        public Iterator getNext() {
            if (currentCell != null)
                return new Iterator(owner, currentCell.getNext());
            return null;
        }

        public Iterator getPrev() {
            if (currentCell != null)
                return new Iterator(owner, currentCell.getPrev());
            return null;
        }

        public boolean hasNext() {
            return currentCell != null && currentCell.getNext() != null;
        }

        public boolean hasPrev() {
            return currentCell != null && currentCell.getPrev() != null;
        }
    }

}