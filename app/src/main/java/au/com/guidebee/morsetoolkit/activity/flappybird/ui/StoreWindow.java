package au.com.guidebee.morsetoolkit.activity.flappybird.ui;

import android.content.Intent;
import android.net.Uri;

import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Table;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;


public class StoreWindow extends BaseWindow {
    public StoreWindow(final FlappyBirdGamePlay gamePlay) {
        super(gamePlay);

        Image image = new Image(uiSkin, "dialog_bg");
        image.setFillParent(true);
        stack.addComponent(image);

        Table table = new Table();
        table.setFillParent(true);
        table.pad(10, 10, 0, 0);
        stack.addComponent(table);

        Image notice = new Image(uiSkin, "notice");
        table.add(notice).colspan(2);
        table.row();

        Button buyButton = new Button(uiSkin, "buy");

        buyButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/guidebee/morsecode"));

                Configuration.gameActivity.startActivity(browserIntent);

                return true;
            }
        });

        table.add(buyButton).left();

        Button backButton = new Button(uiSkin, "back");

        backButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new MainWindow(gamePlay));
                return true;
            }
        });

        table.add(backButton).right();


    }

    @Override
    public void show() {
        gamePlay.hideBannder();
        super.show();
    }
}
