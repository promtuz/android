// dart format off

enum MessageType { content }

class DummyLastMessage {
  final String content;
  final int timestamp;
  final MessageType type;

  const DummyLastMessage(this.content, this.timestamp, { this.type = MessageType.content });
}

class DummyChat {
  final String name;
  final DummyLastMessage lastMessage;

  const DummyChat(this.name, this.lastMessage);
}

const dummyChats = [
  DummyChat("Alice Chen", DummyLastMessage("See you tomorrow!", 1761847076578)),
  DummyChat("Work Team", DummyLastMessage("David: Meeting moved to 3 PM", 1761846656578)),
  DummyChat("Bob Martinez", DummyLastMessage("Did you get the files?", 1761844676578)),
  DummyChat("Mom", DummyLastMessage("Call me when you're free", 1761840176578)),
  DummyChat("Sarah Williams", DummyLastMessage("Thanks for the help 😊", 1761836576578)),
  DummyChat("Gym Buddies", DummyLastMessage("Jake: Who's in for tomorrow 6 AM?", 1761829376578)),
  DummyChat("Mike Johnson", DummyLastMessage("Running late, be there in 10", 1761778976578)),
  DummyChat("Emma Davis", DummyLastMessage("That sounds great!", 1761757376578)),
  DummyChat("College Friends", DummyLastMessage("You: Anyone up for weekend plans?", 1761674576578)),
  DummyChat("Alex Kim", DummyLastMessage("Can we reschedule?", 1761588176578)),
  DummyChat("Lisa Brown", DummyLastMessage("Just finished the meeting", 1761501776578)),
  DummyChat("Dad", DummyLastMessage("How's work going?", 1761415376578)),
  DummyChat("Tom Anderson", DummyLastMessage("Check out this link", 1761328976578)),
  DummyChat("Project Alpha", DummyLastMessage("Nina: Deadline extended by 2 days", 1761156176578)),
  DummyChat("Nina Patel", DummyLastMessage("Happy birthday! 🎉", 1760983376578)),
  DummyChat("Chris Lee", DummyLastMessage("I'll send it over now", 1760810576578)),
  DummyChat("Maya Rodriguez", DummyLastMessage("Perfect timing", 1760551376578)),
  DummyChat("Apartment Neighbors", DummyLastMessage("Sarah: Maintenance tomorrow 9-11 AM", 1760292176578)),
  DummyChat("David Smith", DummyLastMessage("Let me know when you're free", 1760032976578)),
  DummyChat("Sophie Turner", DummyLastMessage("Absolutely!", 1759687376578)),
  DummyChat("Ryan Cooper", DummyLastMessage("Got it, thanks", 1759255376578)),
  DummyChat("Book Club", DummyLastMessage("Emily: Next meeting March 15th", 1758823376578)),
  DummyChat("Zara Ahmed", DummyLastMessage("On my way", 1758391376578)),
  DummyChat("Coffee Meetup", DummyLastMessage("You: Same place next week?", 1757527376578)),
  DummyChat("James Wilson", DummyLastMessage("Congrats on the promotion!", 1756663376578)),
  DummyChat("Olivia Zhang", DummyLastMessage("Safe travels!", 1755367376578)),
  DummyChat("Weekend Hikers", DummyLastMessage("Mark: Trail conditions are perfect", 1754071376578)),
  DummyChat("Rachel Green", DummyLastMessage("Miss you! Let's catch up soon", 1751479376578)),
];