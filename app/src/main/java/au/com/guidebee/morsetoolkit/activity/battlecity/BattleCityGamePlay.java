package au.com.guidebee.morsetoolkit.activity.battlecity;

import com.guidebee.game.GamePlay;

import au.com.guidebee.morsetoolkit.activity.battlecity.actors.Bullet;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.Explosion;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.Powerup;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.Score;
import au.com.guidebee.morsetoolkit.activity.battlecity.actors.tank.Tank;

import static com.guidebee.game.GameEngine.assetManager;


public class BattleCityGamePlay extends GamePlay {
    @Override
    public void create() {
        // load the assets

        ResourceManager.loadResources();
        Powerup.initPowerups();
        Bullet.initBullets();
        Explosion.initExplosions();
        Tank.initTanks();
        Score.initScores();

        BattleCityGameScene.imgGameover=ResourceManager.getInstance().getImage(ResourceManager.GAME_OVER_SMALL);
        BattleCityGameScene.imgPause=ResourceManager.getInstance().getImage(ResourceManager.PAUSE);
        BattleCityGameScene.imgNumberBlack=ResourceManager.getInstance().getImage(ResourceManager.NUMBER_BLACK);
        BattleCityGameScene.imgEnemyIcon=ResourceManager.getInstance().getImage(ResourceManager.ENEMY_ICON);
        BattleCityGameScene.imgIP=ResourceManager.getInstance().getImage(ResourceManager.IP);
        BattleCityGameScene.imgFlag=ResourceManager.getInstance().getImage(ResourceManager.FLAG);
        BattleCityGameScene scene = new BattleCityGameScene();

        setScreen(scene);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
