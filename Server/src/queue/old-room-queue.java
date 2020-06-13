//    protected class RoomQueue extends BasicQueue<TourGroup.QueueTicket> {
//
//        private final Room owner;
//        protected final static int maxAsksForTicket = 3;
//
//        // DO SFORMATOWANIA KODZIK
//        private boolean requestedGrouping;
//        private boolean isDuringGrouping;
//        private boolean isFullyGrouped;
//        //
//
//        private RoomQueue(Room owner) {
//            this.owner = owner;
//            this.isDuringGrouping = false;
//            this.isFullyGrouped = false;
//            this.requestedGrouping = false;
//        }
//
//        protected void updateFullyGroupedStatus() {
//            this.isFullyGrouped = isFullyGrouped();
//        }
//
//        @Override
//        public void enqueue(TourGroup.QueueTicket ticket) {
//            super.enqueue(ticket);
//            if (isDuringGrouping) {
//                getTail().sendNotificationAboutGrouping();
//                return;
//            }
//            tryGrouping();
//        }
//
//        private void sendGroupingStateInformation() {
//            /// wyslij stany ich grupowania do wszystkich ktorzy biora udzial w grupowaniu
//        }
//
//        protected boolean isFullyGrouped() {
//            int maxSlots = owner.maxSlots;
//            int currentAccepted = 0;
//            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
//                if (currentAccepted < maxSlots) {
//                    int response = iter.getData().getGroupingResponse();
//                    if (response == 0)
//                        return false;
//                    if (response == 1)
//                        ++currentAccepted;
//                }
//                if (currentAccepted == maxSlots)
//                    return true;
//            }
//            return false;
//        }
//
//        private boolean shouldDelete(TourGroup.QueueTicket ticket) {
//            return ticket.getTimesAsked() == Room.RoomQueue.maxAsksForTicket;
//        }
//
//        private ArrayList<TourGroup> pullGroups() {
//            int maxSlots = owner.maxSlots;
//            int size = 0;
//            ArrayList<TourGroup> groups = new ArrayList<>();
//            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
//                if (size < maxSlots) {
//                    TourGroup.QueueTicket ticket = iter.getData();
//                    int response = ticket.getGroupingResponse();
//                    if (response == -1 || response == 0) {
//                        ticket.increaseTimesAsked();
//                        if (shouldDelete(ticket))
//                            removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
//                    }
//                    else if (response == 1) {
//                        groups.add(ticket.getOwner());
//                        removeFirstOccurrence(ticket); ///ZMIENIC NA REMOVETICKET!
//                        ++size;
//                    }
//                }
//                if (size == maxSlots)
//                    return groups;
//            }
//            return groups;
//        }
//
//        public boolean removeFirstMatching(Function<TourGroup.QueueTicket, Boolean> matcher) {
//            for (BasicQueue.Iterator it = getIterator(); it.isValid(); it = it.getNext()) {
//                if (matcher.apply((TourGroup.QueueTicket)it.getData()) == Boolean.TRUE) {
//                    this.removeAt(it);
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        private void tryGrouping() {
//            if (isDuringGrouping)
//                return;
//            if (owner.state == State.OPEN) {
//                //////////////////////////////////////////////////////////// ustaw rozpoczecie grupowania (w nowym watku np)
//                //new Thread(this::launchGrouping).start();
//            } else {
//                requestedGrouping = true;
//            }
//        }
//
//        private void launchGrouping() {
//            isDuringGrouping = true;
//
//            for (BasicQueue<TourGroup.QueueTicket>.Iterator iter = this.getIterator(); iter.isValid(); iter = iter.getNext()) {
//                TourGroup.QueueTicket ticket = iter.getData();
//                if (ticket.getOwner().canAddReservation()) {
//                    ticket.sendNotificationAboutGrouping();
//                } else {
//                    ticket.setNoParticipation();
//                }
//            }
//            Date startedGroupingDate = new Date();
//            long finish = startedGroupingDate.getTime() + 10 * 1000;
//
//            /// czekaj na zakonczenie grupowania (zakonczenie czasu albo max liczba grup)
//            while (finish > new Date().getTime() && !isFullyGrouped) {
//                /// opoznienie co sekunde
//                sendGroupingStateInformation();
//            }
//
//            requestedGrouping = false;
//            isDuringGrouping = false;
//            isFullyGrouped = false;
//
//            /// stworz ewentualne rezerwacje dla tych co sie zgodzili
//            ArrayList<TourGroup> groups = pullGroups();
//            if (groups.size() > 0) {
//                changeState(State.RESERVED);
//                for (TourGroup group : groups) {
//                    // co jesli bral udzial w innym grupowaniu i tez sie zgodzil 'w tym samym czasie'? (nie moze dostac dwoch rezerwacji!)
//                    group.addReservation(createReservation(group));
//                }
//                owner.launchReservationTimer();
//            }
//        }
//    }