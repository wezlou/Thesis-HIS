package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class StoriesActivity extends AppCompatActivity {

    private LinearLayout storyContainer;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
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
            "Once upon a time, a hare mocked a slow-moving tortoise. Proud of his speed, the hare challenged the tortoise to a race. As the race began, the hare ran far ahead and, feeling certain of victory, decided to nap under a tree. Meanwhile, the tortoise moved slowly but steadily, never stopping. When the hare awoke, he dashed toward the finish line, but to his surprise, the tortoise had already crossed it. The animals cheered for the tortoise, while the hare learned a lesson in humility. Moral: Slow and steady wins the race.",

            "A young shepherd boy grew bored while watching his sheep in the meadow. To amuse himself, he shouted, 'Wolf! Wolf! A wolf is attacking the flock!' The villagers rushed to help, only to find no wolf. The boy laughed at their anger. Days later, he played the same trick, and again the villagers came running in vain. But one night, a real wolf appeared and attacked the sheep. The boy screamed for help, but no one came, thinking it was another false alarm. The wolf scattered the flock, leaving the boy regretful. Moral: No one believes a liar, even when they tell the truth.",

            "One day in the forest, a mighty lion caught a tiny mouse. The lion raised his paw to crush it, but the mouse pleaded, 'Please spare me! I may be small, but one day I will help you.' Amused, the lion let him go. Not long after, the lion was trapped in a hunter’s net. He roared helplessly. Hearing his cries, the mouse rushed to his aid and gnawed through the ropes until the lion was free. The lion thanked the mouse, realizing even the smallest creatures can be of great help. Moral: Kindness is never wasted.",

            "A hungry fox came across a vine of ripe grapes hanging high above him. He leapt and leapt, but no matter how hard he tried, he could not reach them. Finally, tired and frustrated, the fox turned away, muttering, 'Those grapes are probably sour anyway.' He left, pretending he didn’t want them. Moral: It’s easy to despise what you cannot have.",

            "During the summer, an ant worked hard gathering food for the winter. Nearby, a grasshopper spent his days singing and playing. 'Why not rest and sing with me?' the grasshopper asked. The ant replied, 'I must prepare for winter.' When the cold months came, the ant was warm and had plenty to eat, while the grasshopper, hungry and freezing, begged the ant for food. But the ant said, 'You sang in summer, now dance in winter.' Moral: Hard work and planning bring rewards.",

            "A mother duck watched her eggs hatch. All the ducklings were yellow and fluffy—except one, who was large, gray, and awkward. The others mocked him, calling him ugly. Lonely and sad, the duckling wandered off, enduring a hard winter alone. But when spring came, he saw his reflection in a pond and realized he had grown into a beautiful swan. The other birds admired him, and he finally found where he belonged. Moral: Don’t judge by appearances; true beauty takes time to show.",

            "Three little pigs set out to build homes. The first pig built a house of straw, the second a house of sticks, and the third, a sturdy house of bricks. Soon, a hungry wolf came along. He huffed and puffed and blew down the straw house, then the stick house, but he could not destroy the brick house. Safe inside, the pigs laughed while the wolf fled in defeat. Moral: Hard work and effort create strong foundations.",

            "A little girl named Little Red Riding Hood set off to visit her grandmother, carrying a basket of food. Her mother warned her not to talk to strangers. On the way, a sly wolf approached her, asking where she was going. She innocently told him. The wolf hurried to the grandmother’s house, swallowed the old woman, and disguised himself as her. When Red Riding Hood arrived, she noticed her grandmother looked strange. 'What big eyes you have!' she said. 'The better to see you with,' the wolf replied. Just as the wolf was about to pounce, a woodcutter rushed in and saved them, freeing the grandmother. Moral: Be cautious and never trust strangers.",

            "Goldilocks wandered into the forest and found a cottage. Inside, she saw three bowls of porridge. She tasted the first—too hot. The second—too cold. The third—just right, and she ate it all. Next, she tried three chairs. The first was too big, the second too hard, and the third just right—but it broke under her. Then she lay in three beds: one too hard, one too soft, and the last one just right, where she fell asleep. The bears returned home to find her there. Frightened, Goldilocks fled and never returned. Moral: Respect others’ belongings.",

            "A farmer owned a goose that laid a golden egg every morning. At first, the farmer was delighted, but soon he grew greedy. 'Why wait for one egg a day? If I cut the goose open, I’ll get them all at once!' he thought. But when he killed the goose, he found no treasure inside—only an ordinary bird. He had lost the source of his wealth. Moral: Greed leads to ruin.",

            "Cinderella lived with a cruel stepmother and stepsisters who treated her like a servant. One day, an invitation came for a royal ball. The stepsisters went, but Cinderella was left behind. Her fairy godmother appeared and magically transformed her rags into a beautiful gown, with glass slippers on her feet. She warned Cinderella to return by midnight. At the ball, the prince was captivated by her. But at midnight, she fled, leaving behind a slipper. The prince searched the land, and when he found that the slipper fit only Cinderella, he married her, and she lived happily ever after. Moral: Kindness and patience are rewarded.",

            "Jack lived with his poor mother. One day, he traded their only cow for a handful of magic beans. His mother was furious and threw them away. Overnight, a giant beanstalk grew into the sky. Jack climbed it and found a giant’s castle. Inside, he discovered treasures: a hen that laid golden eggs and a magical harp. The giant chased him, but Jack escaped down the beanstalk and chopped it down. The giant fell, never to return. Jack and his mother lived in comfort. Moral: Courage and cleverness can change your fate.",

            "Hansel and Gretel were siblings lost in the forest. They came upon a cottage made of candy and sweets. Hungry, they began to eat it, but an old witch appeared. She lured them inside and locked Hansel in a cage, planning to fatten him up. Gretel was forced to work. One day, the witch prepared the oven to cook Hansel. Gretel tricked her by pretending not to know how it worked. When the witch bent over to show her, Gretel pushed her inside and shut the door. They escaped, taking the witch’s treasures, and found their way home. Moral: Bravery and quick thinking can overcome evil.",

            "A girl with long, magical hair named Rapunzel was locked in a tower by a wicked sorceress. The only way up was by climbing her hair when the sorceress called, 'Rapunzel, Rapunzel, let down your hair!' One day, a prince overheard and visited her in secret. They fell in love. When the sorceress discovered this, she cut Rapunzel’s hair and banished her. The prince was blinded by thorns while searching for her. Years later, Rapunzel’s tears of love healed his eyes. They were reunited and lived happily ever after. Moral: Love and hope can overcome great trials.",

            "Snow White was a princess whose jealous stepmother, the Queen, asked her magic mirror, 'Who is the fairest of them all?' When the mirror answered, 'Snow White,' the Queen ordered a huntsman to kill her. But he spared Snow White, who fled into the forest. She found a cottage belonging to seven dwarfs who welcomed her. The Queen discovered Snow White was alive and disguised herself three times, finally tricking her with a poisoned apple. Snow White fell into a deep sleep. One day, a prince kissed her, breaking the spell. She awoke, and they lived happily ever after. Moral: Jealousy destroys, but goodness triumphs.",

            "Pinocchio was a wooden puppet created by Geppetto. A fairy brought him to life, telling him he could become a real boy if he proved himself brave and honest. But Pinocchio was mischievous, often lying, and each lie made his nose grow longer. He fell into many troubles, including being tricked by sly characters and trapped in dangerous places. At last, he learned to be truthful and selfless, even rescuing Geppetto from danger. The fairy rewarded him by turning him into a real boy. Moral: Honesty and courage lead to a true life.",

            "One day, a little red hen found some wheat seeds. She asked her friends, the cat, the dog, and the duck, 'Who will help me plant this wheat?' 'Not I,' they each replied. She planted it alone. Later, she asked who would help water, harvest, and bake bread from the wheat, but again they refused. Finally, when the bread was ready, they all wanted to eat it. But the hen said, 'I did it all myself, so I will eat it myself.' And she did. Moral: Those who do not work should not expect rewards.",

            "A poor fisherman caught a magical fish who begged for freedom. In return, the fish granted wishes. The fisherman’s wife grew greedy, asking for wealth, then a castle, then to be queen. Each time, the fish granted her wish, but she was never satisfied. Finally, she demanded to be ruler of the seas. The fish grew angry and took back everything, leaving them poor again. Moral: Greed destroys happiness.",

            "A country mouse invited his cousin, the town mouse, to visit. He offered him plain but hearty food. The town mouse laughed and invited him to the city, where they feasted on rich food in a grand house. But their meal was interrupted by a cat, forcing them to run for their lives. Terrified, the country mouse returned home, saying, 'Better beans in peace than delicacies in fear.' Moral: A simple life in safety is better than a luxurious one in danger.",

            "The colors of the rainbow once quarreled over who was most important. Green boasted of life and growth, blue of the sky and sea, red of passion, yellow of warmth, and so on. Suddenly, rain poured down, and lightning flashed. The colors huddled together in fear. Then the rain spoke, 'Stop fighting. Each of you is special, but together you create beauty. When the storm ends, you will shine as one.' And so the rainbow appeared, dazzling the world. Moral: Unity creates harmony and beauty."
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

        // ✅ Background music (looping)
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        storyContainer = findViewById(R.id.story_container);

        ImageView backButton = findViewById(R.id.backButton);
        ImageView settingsButton = findViewById(R.id.settingsButton);

        // Back navigates to Categorypage (same behavior as Videos)
        backButton.setOnClickListener(v -> {
            stopMusic();
            Intent intent = new Intent(StoriesActivity.this, Categorypage.class);
            startActivity(intent);
            finish();
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

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

        innerLayout.addView(imageView);
        innerLayout.addView(titleView);
        innerLayout.addView(previewView);
        card.addView(innerLayout);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(StoriesActivity.this, StoryDetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("image", imageRes);
            startActivity(intent);
        });

        storyContainer.addView(card);
    }

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("Mute")) {
                muteDevice();
                isMuted = true;
                Toast.makeText(this, "Muted 🔇", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
                Toast.makeText(this, "Unmuted 🔊", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Exit")) {
                stopMusic();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }

    private void muteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0f, 0f);
        }
    }

    private void unmuteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1f, 1f);
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !isMuted && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
