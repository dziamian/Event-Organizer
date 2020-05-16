package queue;

public class BasicQueue<T> {

    protected QueueCell head;
    protected QueueCell tail;
    private int currentSize;

    public BasicQueue() {
        this.currentSize = 0;
    }

    public T getHead() {
        return head.data;
    }

    public T getTail() {
        return tail.data;
    }

    public int size() {
        return currentSize;
    }

    public void enqueue(T data) {
        addBack(data);
    }

    public T dequeue() {
        if (head != null) {
            T data = head.getData();
            removeCell(head);
            return data;
        }
        return null;
    }

    public boolean removeAt(Iterator where) {
        if (where != null && where.isValid()) {
            removeCell(where.currentCell);
            return true;
        }
        return false;
    }

    protected void addBack(T data) {
        QueueCell newCell = new QueueCell(data, null, tail);
        if (tail != null)
            tail.setNext(newCell);
        tail = newCell;
        if (head == null)
            head = newCell;
        ++currentSize;
    }

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