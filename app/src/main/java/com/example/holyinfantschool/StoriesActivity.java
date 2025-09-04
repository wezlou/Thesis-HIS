package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class StoriesActivity extends AppCompatActivity {

    private LinearLayout storyContainer;

    private String[] storyTitles = {
            "The Tortoise and the Hare",
            "The Boy Who Cried Wolf",
            "The Lion and the Mouse",
            "The Fox and the Grapes",
            "The Ant and the Grasshopper",
            "The Ugly Duckling",
            "The Three Little Pigs",
            "Little Red Riding Hood",
            "Goldilocks and the Three Bears",
            "The Goose That Laid Golden Eggs",
            "Cinderella",
            "Jack and the Beanstalk",
            "Hansel and Gretel",
            "Rapunzel",
            "Snow White",
            "Pinocchio",
            "The Little Red Hen",
            "The Fisherman and His Wife",
            "The Town Mouse and the Country Mouse",
            "The Rainbow’s Lesson"
    };

    private String[] storyContents = {
            // 1 The Tortoise and the Hare
            "Once upon a time, a hare made fun of the slow-moving tortoise. Tired of his arrogance, the tortoise challenged him to a race. The hare laughed but agreed. During the race, the hare sped ahead and, thinking he was safe, decided to nap. The tortoise kept moving slowly but steadily and eventually passed the sleeping hare. When the hare awoke, it was too late. The tortoise had won. Moral: Slow and steady wins the race.",

            // 2 The Boy Who Cried Wolf
            "There once was a shepherd boy who grew bored watching his flock. To amuse himself, he cried out, 'Wolf! Wolf!' The villagers came running, only to find no wolf. The boy laughed. He repeated this trick several times until the villagers stopped believing him. One day, a real wolf came and attacked the flock. The boy cried for help, but no one came. Moral: Liars are not believed even when they speak the truth.",

            // 3 The Lion and the Mouse
            "A lion once spared a tiny mouse who had disturbed him. Later, the lion was caught in a hunter’s net. The little mouse came and gnawed through the ropes, freeing the lion. The lion realized even the smallest creatures can be of help. Moral: Kindness is never wasted.",

            // 4 The Fox and the Grapes
            "A hungry fox saw some grapes hanging high on a vine. He jumped and jumped but could not reach them. Finally, he gave up and muttered, 'They’re probably sour anyway.' Moral: It’s easy to despise what you cannot have.",

            // 5 The Ant and the Grasshopper
            "All summer long, the ant worked hard gathering food, while the grasshopper sang and played. When winter came, the grasshopper had nothing to eat and begged the ant for food. The ant reminded him of his laziness. Moral: Work hard today to prepare for tomorrow.",

            // 6 The Ugly Duckling
            "A mother duck’s eggs hatched, and one duckling looked different—big and awkward. The other animals mocked him. Sad and lonely, he wandered until one day he grew into a beautiful swan. Moral: Don’t judge by appearances; true beauty takes time to show.",

            // 7 The Three Little Pigs
            "Three pigs built houses: one of straw, one of sticks, and one of bricks. A hungry wolf blew down the first two houses, but the brick house stood strong. The wolf could not get in. Moral: Hard work and planning bring security.",

            // 8 Little Red Riding Hood
            "A little girl went to visit her grandmother, carrying a basket of food. A cunning wolf reached the grandmother’s house first, disguised himself, and tricked the girl. Just as he was about to eat her, a woodcutter saved her. Moral: Be cautious of strangers.",

            // 9 Goldilocks and the Three Bears
            "Goldilocks entered the bears’ house while they were away. She ate their porridge, sat in their chairs, and slept in their beds. When the bears returned, she fled in fright. Moral: Respect others’ property.",

            // 10 The Goose That Laid Golden Eggs
            "A farmer owned a goose that laid a golden egg each day. Greedy for more, he killed the goose, hoping to get all the gold inside at once. But there was nothing. Moral: Greed destroys what you already have.",

            // 11 Cinderella
            "Cinderella lived with a cruel stepmother and stepsisters. With the help of her fairy godmother, she attended the prince’s ball but had to leave at midnight. The prince searched for the girl who fit the glass slipper, and Cinderella became his bride. Moral: Kindness and patience are rewarded.",

            // 12 Jack and the Beanstalk
            "Jack traded his cow for magic beans. His mother was angry, but the beans grew into a huge beanstalk reaching the sky. Jack climbed it and found a giant’s castle full of treasures. He escaped with riches and lived happily with his mother. Moral: Courage and cleverness bring rewards.",

            // 13 Hansel and Gretel
            "Lost in the forest, Hansel and Gretel found a candy house owned by a wicked witch. She trapped them, planning to eat them, but clever Gretel tricked her and pushed her into the oven. The children escaped with treasure. Moral: Bravery and wit can overcome evil.",

            // 14 Rapunzel
            "A girl with long magical hair was locked in a tower by a wicked witch. A prince found her and climbed her hair to visit. They fell in love and eventually escaped, defeating the witch. Moral: Love and perseverance can overcome obstacles.",

            // 15 Snow White
            "Snow White’s jealous stepmother gave her a poisoned apple. She fell into a deep sleep until a prince kissed her, breaking the spell. She awoke and lived happily ever after. Moral: Goodness and purity triumph over envy.",

            // 16 Pinocchio
            "A wooden puppet named Pinocchio wanted to be a real boy. Each time he lied, his nose grew longer. Through honesty and bravery, he finally became human. Moral: Honesty is the best policy.",

            // 17 The Little Red Hen
            "A little red hen found some wheat and asked her friends to help plant it. They refused. She harvested, milled, and baked bread alone. When it was ready, they all wanted to eat, but she refused to share. Moral: You reap what you sow.",

            // 18 The Fisherman and His Wife
            "A poor fisherman caught a magical fish who granted wishes. His wife kept asking for more—first a house, then a castle, then to be queen. Finally, she wanted to be God. The fish took everything back, and they returned to poverty. Moral: Greed leads to downfall.",

            // 19 The Town Mouse and the Country Mouse
            "The country mouse visited his cousin in the city. The city mouse had luxury but constant danger, while the country mouse lived simply but safely. The country mouse returned home, preferring peace over wealth. Moral: Better a simple life in safety than luxury in fear.",

            // 20 The Rainbow’s Lesson
            "One day, the colors of the rainbow argued about who was most important. Each boasted of their beauty and use. Then rain came, and they joined together to form a magnificent rainbow. They realized their differences made them stronger together. Moral: Unity in diversity."
    };

    private int[] storyImages = {
            R.drawable.tortoise_hare,
            R.drawable.boy_wolf,
            R.drawable.lion_mouse,
            R.drawable.fox_grapes,
            R.drawable.ant_grasshopper,
            R.drawable.ugly_duckling,
            R.drawable.three_pigs,
            R.drawable.red_riding_hood,
            R.drawable.goldilocks,
            R.drawable.goose_eggs,
            R.drawable.cinderella,
            R.drawable.jack,
            R.drawable.hansel_gretel,
            R.drawable.rapunzel,
            R.drawable.snow_white,
            R.drawable.pinocchio,
            R.drawable.red_hen,
            R.drawable.fisherman_wife,
            R.drawable.town_mouse,
            R.drawable.rainbow_lesson
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        storyContainer = findViewById(R.id.story_container);

        for (int i = 0; i < storyTitles.length; i++) {
            addStoryCard(storyTitles[i], storyContents[i], storyImages[i]);
        }
    }

    private void addStoryCard(String title, String content, int imageRes) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setRadius(20);
        card.setCardElevation(8);
        card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        card.setUseCompatPadding(true);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        // Image
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(imageRes);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
        );
        imageView.setLayoutParams(imageParams);

        // Title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));

        // Preview
        TextView previewView = new TextView(this);
        previewView.setText(content.length() > 80 ? content.substring(0, 80) + "..." : content);
        previewView.setTextSize(16);
        previewView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        // Add views
        innerLayout.addView(imageView);
        innerLayout.addView(titleView);
        innerLayout.addView(previewView);
        card.addView(innerLayout);

        // On click → open detail page
        card.setOnClickListener(v -> {
            Intent intent = new Intent(StoriesActivity.this, StoryDetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("image", imageRes);
            startActivity(intent);
        });

        storyContainer.addView(card);
    }
}
