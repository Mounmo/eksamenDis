package utils;


public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better: FIX
      char[] key = Config.getKEY().toCharArray();

      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?: FIX
      /**
       * For-løkken fortsætter med at køre, indtil der ikke er flere karakterer i rawString.
       * Der er blevet specificeret, at følgende skal ske inde i forløkken;
       * Ved brug af ”^” omdannes den karakter som er placeret ved i’s plads i rawstring til en binær værdi.
       * Derefter tages den binære værdi af karakteren, som er placeret på i’s plads i charArray key.
       * De to binære værdier lægges sammen ved brug og XOR.
       * Der returneres derfor en helt ny binær værdi. Denne værdi omdannes derefter til en char, og den tilføjes til
       * thisIsEncrypted ved brug af thisIsEncrypted.append.
       * I tilfældet af, at længden på charArray key er mindre end længden på rawString, er der ved brug af
       * modulus-operatoren gjort muligt, at når slutningen af charArray key er nået, så startes den om.
       */
      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
