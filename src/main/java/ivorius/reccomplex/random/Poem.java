/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import com.google.common.io.LineReader;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.CustomizableMap;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by lukas on 25.06.14.
 */
public class Poem
{
    public static final List<String> sentencePatterns = Arrays.asList(
            "As the <10> are, the <10> <4> <7>.",
            "All the <10> <3> <6>, so <5> the <10>.",
            "<9>! We <4> the <1> and the <2>, why not <5>?",
            "What is the <6> <2> to <7> <3> the <1>?",
            "To <3>, we <4>. To <3>, we <4>.",
            "With <2>, one can <5> oh so <7>...",
            "Why do the <10> <3> <2> so <7>?",
            "They <3> to <3>.",
            "Is <2> really <6> like <2>?",
            "Could you <3> it so <7>?",
            "The <2> of <lownum> <10>",
            "We <4> to <3>.",
            "When <1> <4>,",
            "Sometimes <6> and always <6>.",
            "<9>! <2>, the <6> <2>.",
            "The <6> <1> <7> <3>s a <1>.",
            "<7> <6>, <10> <7> <3> a <6> <1>.",
            "<10> <4> the <1> <8> the <6> <2>, <7> but <7>.",
            "<1> and <1>, <9>, <9>! <10> like the <2>.",
            "<10> <5> and <8> them <2> <5>s!",
            "Only the <1> <5>s as a <6> <1>.",
            "<10> -- <6> <10>!",
            "<2> is a <6> <1> <8> <2> and <2>.",
            "<9> <10>...",
            "<9>!",
            "We <5>, but only for a while,",
            "<7>, <7>, <7>.",
            "And <5> <7>, ",
            "I will <3> <8> the <10>",
            "But when the <10> <5>",
            "When do <10> become <10>?",
            "<7>, <10> <5>",
            "Count the <10>, <3> the <2>.",
            "How does the <1> not <5>?",
            "Although it's now more <6> and less <6>.",
            "Yet there's <2> <8> the <2>s and the <10>.",
            "<10> <4> <10> like <6> <10> <5>.",
            "Why did the <1> <3> it, to <3> the <2>?",
            "<2>, <2>, and ever <2>.",
            "Where was the <6> <2> then?",
            "<2> like <highnum> <10>",
            "Where did our <2> go? When did we lose the <2>?",
            "<10>, <10>, <10> - Those are our true <2>.",
            "<8> or <8>, how <2> <5>s on.",
            "<5> <7> like a <6> <1> <8> <2>.",
            "Where is the <6> <1>, the <6> <2> now?",
            "What is <2> after all...",
            "They never <3> the <10> nor the <10>, with <2>.",
            "<5 r>, <name>! <5 r>!",
            "No <1> is <6>, no <1> is <6>.",
            "Oh so <6>",
            "<name>",
            "<name> is <6>",
            "Ever to <3> a <1>, it <4> a <1>.",
            "<name> is <6 r>, oh so <6 r>",
            "So <6>",
            "<name>!",
            "<name r>! Oh, <name r>!",
            "<name>! It is you!",
            "<5>, <name>!",
            "We <4> the <place> before we <4> <2>.",
            "<3> the <place> <8> you, <name>!",
            "<9>, how the <place> <7> <5>s",
            "<lownum> <10>",
            "<highnum> <10>",
            "<hugenum> <10>",
            "<lownum> <10> <5> <7>",
            "<highnum> <10> <5> <7>",
            "<hugenum> <10> <5> <7>",
            "These <10> I <3>",
            "These <10> I once <4>",
            "I <5> every day",
            "Now the <2> is <6>",
            "<8> <10> of <2>",
            "<8> <10> of <2> and <10> of <2>",
            "Where <10> <5>"
    );

    private static final CustomizableMap<String, Theme> themes = new CustomizableMap<>();

    private String title;
    private String text;

    public Poem(String title, String text)
    {
        this.title = title;
        this.text = text;
    }

    public static void registerTheme(String name, Theme theme, boolean custom)
    {
        if (!RCConfig.disablingsomeloggings){
        String baseString = themes.put(name, theme, custom) != null ? "Replaced poem theme '%s'" : "Registered poem theme '%s'";
        RecurrentComplex.logger.info(String.format(baseString, name));
    }
    }
    public static void unregisterTheme(String name, boolean custom)
    {
        themes.remove(name, custom);
    }

    public static void clearCustom()
    {
        themes.clearCustom();
    }

    public static Poem randomPoem(Random random)
    {
        return randomPoem(random, getRandomElementFrom(Arrays.asList(themes.getMap().values().toArray(new Theme[themes.getMap().size()])), random));
    }

    public static Poem randomPoem(Random random, Theme theme)
    {
        PoemContext poemContext = new PoemContext();
        while (poemContext.add(random, poemContext.names, 0.3f, Person.randomHuman(random, random.nextBoolean()).getFirstName()))
            ;
        while (poemContext.add(random, poemContext.places, 0.3f, Place.randomPlace(random).getFullPlaceType())) ;

        String title = getRandomPhrase(random, theme, sentencePatterns, poemContext).trim();
        char titleLastChar = title.charAt(title.length() - 1);
        if (titleLastChar == '.' || titleLastChar == ',' || titleLastChar == ';')
            title = title.substring(0, title.length() - 1);

        StringBuilder poem = new StringBuilder();

        int verses = random.nextInt(5) + 1;
        for (int verse = 0; verse < verses; verse++)
        {
            int lines = random.nextInt(10) + 1;
            for (int line = 0; line < lines; line++)
            {
                String phrase = getRandomPhrase(random, theme, sentencePatterns, poemContext);

                if (line == lines - 1)
                {
                    char phraseLastChar = phrase.charAt(phrase.length() - 1);
                    if (phraseLastChar == ',' || phraseLastChar == ';')
                        phrase = phrase.substring(0, phrase.length() - 1) + ".";
                }

                poem.append(phrase).append("\n");
            }

            poem.append("\n");
        }

        return new Poem(title, poem.toString());
    }

    private static String getRandomPhrase(Random random, Theme theme, List<String> sentencePatterns, PoemContext poemContext)
    {
        return firstCharUppercase(replaceAllWords(random, getRandomElementFrom(sentencePatterns, random), theme, poemContext));
    }

    private static String replaceAllWords(Random random, String text, Theme theme, PoemContext poemContext)
    {
        StringBuilder builder = new StringBuilder(text);

        replaceAll(random, builder, "10", theme.concreteNounsPlural);
        replaceAll(random, builder, "1", theme.concreteNouns);
        replaceAll(random, builder, "2", theme.abstractNouns);
        replaceAll(random, builder, "3", theme.transitivePresentVerbs);
        replaceAll(random, builder, "4", theme.transitivePastVerbs);
        replaceAll(random, builder, "5", theme.intransitivePresentVerbs);
        replaceAll(random, builder, "6", theme.adjectives);
        replaceAll(random, builder, "7", theme.adverbs);
        replaceAll(random, builder, "8", theme.prepositions);
        replaceAll(random, builder, "9", theme.interjections);
        replaceAll(random, builder, "name", poemContext.names);
        replaceAll(random, builder, "place", poemContext.places);
        replaceAllWithNums(random, builder, "lownum", 2, 10, 1);
        replaceAllWithNums(random, builder, "highnum", 2, 10, 10);
        replaceAllWithNums(random, builder, "hugenum", 1, 10, 1000);

        return builder.toString();
    }

    private static void replaceAll(Random random, StringBuilder builder, String tag, List<String> words)
    {
        String repeatWord = null;
        int index;
        while ((index = builder.indexOf("<" + tag)) >= 0)
        {
            int endIndex = builder.indexOf(">", index);
            if (builder.charAt(endIndex - 1) == 'r')
            {
                if (repeatWord == null)
                {
                    repeatWord = getRandomElementFrom(words, random);
                }

                builder.replace(index, endIndex + 1, repeatWord);
            }
            else
            {
                builder.replace(index, endIndex + 1, getRandomElementFrom(words, random));
            }
        }
    }

    private static void replaceAllWithNums(Random random, StringBuilder builder, String tag, int min, int max, int mul)
    {
        String repeatWord = null;
        int index;
        while ((index = builder.indexOf("<" + tag)) >= 0)
        {
            int endIndex = builder.indexOf(">", index);
            if (builder.charAt(endIndex - 1) == 'r')
            {
                if (repeatWord == null)
                {
                    repeatWord = String.valueOf((random.nextInt(max - min + 1) + min) * mul);
                }

                builder.replace(index, endIndex + 1, repeatWord);
            }
            else
            {
                builder.replace(index, endIndex + 1, String.valueOf((random.nextInt(max - min + 1) + min) * mul));
            }
        }
    }

    private static String firstCharUppercase(String name)
    {
        return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    public String getTitle()
    {
        return title;
    }

    public String getText()
    {
        return text;
    }

    public static class Theme
    {
        public List<String> concreteNouns = new ArrayList<>();
        public List<String> concreteNounsPlural = new ArrayList<>();
        public List<String> abstractNouns = new ArrayList<>();
        public List<String> transitivePresentVerbs = new ArrayList<>();
        public List<String> transitivePastVerbs = new ArrayList<>();
        public List<String> intransitivePresentVerbs = new ArrayList<>();
        public List<String> adjectives = new ArrayList<>();
        public List<String> adverbs = new ArrayList<>();
        public List<String> prepositions = new ArrayList<>();
        public List<String> interjections = new ArrayList<>();

        public static Theme fromFile(String fileContents)
        {
            Theme theme = new Theme();
            LineReader reader = new LineReader(new StringReader(fileContents));

            List<String> currentList = null;
            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("***"))
                    {
                        String tag = line.substring(4).trim();
                        switch (tag)
                        {
                            case "Concrete Nouns":
                                currentList = theme.concreteNouns;
                                break;
                            case "Concrete Nouns Plural":
                                currentList = theme.concreteNounsPlural;
                                break;
                            case "Abstract Nouns":
                                currentList = theme.abstractNouns;
                                break;
                            case "Present Transitive Verbs":
                                currentList = theme.transitivePresentVerbs;
                                break;
                            case "Past Transitive Verbs":
                                currentList = theme.transitivePastVerbs;
                                break;
                            case "Present Intransitive Verbs":
                                currentList = theme.intransitivePresentVerbs;
                                break;
                            case "Adjectives":
                                currentList = theme.adjectives;
                                break;
                            case "Adverbs":
                                currentList = theme.adverbs;
                                break;
                            case "Prepositions":
                                currentList = theme.prepositions;
                                break;
                            case "Interjections":
                                currentList = theme.interjections;
                                break;
                            default:
                                currentList = null;
                        }
                    }
                    else
                    {
                        String word = line.trim();
                        if (word.length() > 0 && currentList != null)
                        {
                            currentList.add(word);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                RecurrentComplex.logger.error(e);
            }

            assertContents(theme.concreteNouns);
            assertContents(theme.abstractNouns);
            assertContents(theme.transitivePresentVerbs);
            assertContents(theme.transitivePastVerbs);
            assertContents(theme.intransitivePresentVerbs);
            assertContents(theme.adjectives);
            assertContents(theme.adverbs);
            assertContents(theme.prepositions);
            assertContents(theme.interjections);

            return theme;
        }

        private static void assertContents(List<String> list)
        {
            if (list.size() == 0)
            {
                list.add("MISSING");
            }
        }
    }

    private static class PoemContext
    {
        public List<String> names = new ArrayList<>();
        public List<String> places = new ArrayList<>();


        public boolean add(Random random, List<String> list, float continueChance, String value)
        {
            list.add(value);
            return random.nextFloat() < continueChance;
        }
    }
}
