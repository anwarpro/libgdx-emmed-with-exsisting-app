package com.teamblackhole.realtimechat.embedlibgdx.basketball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

public class Basketball extends ApplicationAdapter implements GestureDetector.GestureListener, ContactListener {
    private static final float BALL_RADIOS = 0.5f;
    private static final float GROUND_Y = 0.5f;
    private static final float RIM_RADIOS = 0.02f;
    private static final float UPPER_GROUND_Y = 3f;
    final float VIRTUAL_HEIGHT = 8f;
    private final TweenManager manager;
    private final GameData data;

    OrthographicCamera cam;
    SpriteBatch batch;
    Texture texture;

    ResolutionFileResolver fileResolver; // +++

    float gravity = -9.81f; // +++ earths gravity is around 9.81 m/s^2 downwards

    private World world;
    //    private Box2DDebugRenderer debugRender;
    private Body ballBody;
    private Body leftBody;
    private Body rightBody;
    private Fixture groundFix;
    private Fixture groundFixTop;
    private Body groundBody;
    private boolean topOfBasket;
    private Vector3 point;
    private boolean wasTouched;
    private Vector3 point2;
    private boolean shoot;
    private boolean win;

    private Sound dropSound1;
    private Sound shootSound;
    private Sound croowedSound;
    private Sprite spriteBall;
    private Sprite spriteFloor;
    private Sprite spriteWall;
    private Sprite spriteTopMonitor;
    private Sprite spriteSideMonitor;
    private Sprite spriteBasketRim;
    private Sprite spriteBasketBack;

    public static final int STEEL = 0;
    public static final int WOOD = 1;
    public static final int RUBBER = 2;
    public static final int STONE = 3;

    private int ballRemain = 3;
    private int ballStored = 0;
    private int round = 1;
    private int score = 0;

    private Body basketSensor;
    private Sprite[] spriteBallJar;
    private BitmapFont font12;
    private OrthographicCamera uiCam;
    private Sprite currentJar;
    private Sprite currentContainer;
    private Sprite[] winEmoji;
    private boolean drawEmoji;
    private Tween emoJiAnimatino;
    private Sprite[] spriteBallContainer;
    private Sprite spriteGem;
    private boolean drawGem;
    private Tween gemAnimation;
    private Sprite currentJarBottom;
    private Body leftLine;
    private Body rightLine;
    private boolean gameOver;
    private float xpos = 0;
    private int retry = 0;
    private Tween leftInJar;
    private Tween topDownContainer;
    private int number = 0;
    private Sprite spriteHintCircle;
    private Sprite spriteHintArrow;
    private Sprite spriteGameOver;
    private boolean moving;
    private Tween flashHintAnim;

    public Basketball() {
        manager = new TweenManager();
        Tween.registerAccessor(Sprite.class, new SpriteAccessor());
        data = GameData.getInstance();
    }

    private static FixtureDef makeFixture(int material, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        switch (material) {
            case 0:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0.3f;
                fixtureDef.restitution = 0.1f;
                break;
            case 1:
                fixtureDef.density = 0.5f;
                fixtureDef.friction = 0.7f;
                fixtureDef.restitution = 0.3f;
                break;
            case 2:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0f;
                fixtureDef.restitution = 1f;
                break;
            case 3:
                fixtureDef.density = 1f;
                fixtureDef.friction = 0.9f;
                fixtureDef.restitution = 0.01f;
            default:
                fixtureDef.density = 7f;
                fixtureDef.friction = 0.5f;
                fixtureDef.restitution = 0.3f;
        }
        return fixtureDef;
    }

    private Body makeChainShape(World world, Body body, boolean increment) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.angle = 0;

        Body bodyCreate = world.createBody(bodyDef);

        EdgeShape es = new EdgeShape();
        es.set(increment ? body.getPosition().x + 0.07f : body.getPosition().x - 0.07f, body.getPosition().y - 0.8f,
                body.getPosition().x, body.getPosition().y - 0.1f);

        FixtureDef fixtureDef = makeFixture(STEEL, es);
        fixtureDef.isSensor = true;

        bodyCreate.createFixture(fixtureDef);

        return bodyCreate;
    }

    private void createBall() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(xpos, 3f);

        ballBody = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(BALL_RADIOS);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.000001f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.5f; // Make it bounce a little bit
        ballBody.createFixture(fixtureDef);

        shoot = false;
    }

    private void createFloor() {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(cam.viewportWidth / 2f - BALL_RADIOS, 6f);

        leftBody = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        FixtureDef fixtureDef = new FixtureDef();
        circle.setRadius(RIM_RADIOS);
        fixtureDef.shape = circle;
        fixtureDef.density = 0f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.2f; // Make it bounce a little bit
        fixtureDef.isSensor = true;
        leftBody.createFixture(fixtureDef);

        bodyDef.position.set(leftBody.getPosition().x + 2 * BALL_RADIOS, leftBody.getPosition().y);
        rightBody = world.createBody(bodyDef);
        rightBody.createFixture(fixtureDef);

        leftBody.setUserData("beep");
        rightBody.setUserData("beep");


        // Create our body definition
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.KinematicBody;
        groundBodyDef.position.set(new Vector2(0, 0));
        groundBody = world.createBody(groundBodyDef);

        groundBody.setUserData("beep");

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(cam.viewportWidth, 0.4f);

        groundFix = groundBody.createFixture(makeFixture(WOOD, groundBox));

        Body groundBodyTop = world.createBody(groundBodyDef);
        groundBox.setAsBox(cam.viewportWidth, UPPER_GROUND_Y);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.density = 0;
        fixtureDef1.isSensor = true;
        fixtureDef1.shape = groundBox;
        groundFixTop = groundBodyTop.createFixture(fixtureDef1);

        groundBodyTop.setUserData("beep");

        //create basket sensor

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(leftBody.getPosition().x + BALL_RADIOS + 0.008f, leftBody.getPosition().y - 0.3f);
        basketSensor = world.createBody(bodyDef);

        groundBox.setAsBox(BALL_RADIOS - 0.06f,
                0.05f);
        fixtureDef1.shape = groundBox;
        basketSensor.createFixture(fixtureDef1);
        basketSensor.setUserData("BASKET");

        // Create our body definition
        BodyDef basketSensorBodyDef = new BodyDef();
        basketSensorBodyDef.type = BodyDef.BodyType.StaticBody;
        basketSensorBodyDef.position.set(new Vector2(0, leftBody.getPosition().y));

        Body sensor = world.createBody(basketSensorBodyDef);
        sensor.setUserData("basketline");
        PolygonShape groundBoxS = new PolygonShape();
        groundBoxS.setAsBox(cam.viewportWidth, 0.01f);

        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.density = 0;
        fixtureDef2.isSensor = true;
        fixtureDef2.shape = groundBoxS;
        sensor.createFixture(fixtureDef2);
    }

    private void loadAsset() {
        texture = new Texture(fileResolver.resolve("ball.png")); // +++
        spriteBall = new Sprite(texture);

        spriteHintCircle = new Sprite(new Texture(fileResolver.resolve("arrow.png")));
//        spriteHintArrow = new Sprite(new Texture(fileResolver.resolve("Yellow-Arrow.png")));
//        spriteHintArrow.flip(false, true);

        spriteGameOver = new Sprite(new Texture(fileResolver.resolve("pause.jpg")));

        spriteBasketRim = new Sprite(new Texture(fileResolver.resolve("new/busket-transprans.png")));

        spriteFloor = new Sprite(new Texture(fileResolver.resolve("new/flor.png")));
        spriteWall = new Sprite(new Texture(fileResolver.resolve("new/wall.png")));
        spriteTopMonitor = new Sprite(new Texture(fileResolver.resolve("new/monitor.png")));
        spriteSideMonitor = new Sprite(new Texture(fileResolver.resolve("new/monitor2.png")));
        spriteBasketBack = new Sprite(new Texture(fileResolver.resolve("new/basket-background.png")));
        spriteGem = new Sprite(new Texture(fileResolver.resolve("gem.png")));

        spriteBallJar = new Sprite[4];

        for (int i = 0; i < 4; i++) {
            spriteBallJar[i] = new Sprite(new Texture(fileResolver.resolve("basketjar/" + i + ".png")));
            spriteBallJar[i].setY(3.4f);
            spriteBallJar[i].setX(0.2f);
        }

        spriteBallContainer = new Sprite[4];

        for (int i = 0; i < 4; i++) {
            spriteBallContainer[i] = new Sprite(new Texture(fileResolver.resolve("basketjar/" + i + ".png")));
            spriteBallContainer[i].setY(1f + BALL_RADIOS);
            spriteBallContainer[i].setX(0.1f);
        }

        winEmoji = new Sprite[5];
        for (int i = 0; i < 5; i++) {
            winEmoji[i] = new Sprite(new Texture(
                    fileResolver.resolve("emoji/win" + i + ".png")));
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/The Happiness.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        font12 = generator.generateFont(parameter); // font size 12 pixels
        generator.dispose(); // don't forget to dispose to avoid memory leaks!

    }

    public void create() {

        fileResolver = new ResolutionFileResolver(new InternalFileHandleResolver(), new Resolution(800, 480, "480"), // +++
                new Resolution(1280, 720, "720"), new Resolution(1920, 1080, "1080")); // +++
        batch = new SpriteBatch();

        loadAsset();

        cam = new OrthographicCamera();
        cam.setToOrtho(false, VIRTUAL_HEIGHT * Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight(), VIRTUAL_HEIGHT); // +++

        xpos = cam.viewportWidth / 2f;

        uiCam = new OrthographicCamera();
        uiCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCam.position.set(uiCam.viewportWidth / 2f, uiCam.viewportHeight / 2f, 0);

        world = new World(new Vector2(0, gravity), true);
//        debugRender = new Box2DDebugRenderer();

        createBall();
        createFloor();

        leftLine = makeChainShape(world, leftBody, true);
        rightLine = makeChainShape(world, rightBody, false);

        soundLoad();

        Gdx.input.setInputProcessor(new GestureDetector(this));
        world.setContactListener(this);

        if (data.isHint()) {
            flashHintAnim = Tween.from(spriteHintCircle, SpriteAccessor.TYPY_ALPHA, 1f)
                    .target(0f)
                    .ease(TweenEquations.easeNone)
                    .repeat(Tween.INFINITY, 1f)
                    .start(manager);
        }

    }

    private void soundLoad() {
        dropSound1 = Gdx.audio.newSound(Gdx.files.internal("audio/drop.ogg"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/shoot.ogg"));
        croowedSound = Gdx.audio.newSound(Gdx.files.internal("audio/applause.ogg"));
    }

    public void resize(int width, int height) {
        cam.setToOrtho(false, VIRTUAL_HEIGHT * width / (float) height, VIRTUAL_HEIGHT); // +++
        batch.setProjectionMatrix(cam.combined);
        cam.update();
    }

    private static int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void render() {

        cam.update();
        manager.update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float r = ballBody.getFixtureList().get(0).getShape().getRadius();

        batch.begin();

        batch.draw(spriteFloor, 0, 0, cam.viewportWidth, UPPER_GROUND_Y + 0.5f);
        batch.draw(spriteWall, 0, UPPER_GROUND_Y + 0.5f,
                cam.viewportWidth, 8 - (UPPER_GROUND_Y + 0.5f));

        spriteTopMonitor.setSize(cam.viewportWidth - (2 * 0.5f), 0.8f);
        spriteTopMonitor.setPosition(0.5f, 8 - 1.3f);

        batch.draw(spriteTopMonitor, spriteTopMonitor.getX(), spriteTopMonitor.getY(),
                spriteTopMonitor.getWidth(), spriteTopMonitor.getHeight());

        batch.setProjectionMatrix(uiCam.combined);
        batch.draw(spriteSideMonitor, 20, uiCam.viewportHeight / 2f + (uiCam.viewportHeight / 2f / 2f),
                80, 46);
        font12.draw(batch, String.format("%02d", score), 40, uiCam.viewportHeight / 2f + (uiCam.viewportHeight / 2f / 2f) + 28);
        batch.setProjectionMatrix(cam.combined);

        if (drawEmoji) {
            batch.draw(winEmoji[number], winEmoji[number].getX(),
                    spriteTopMonitor.getY() + spriteTopMonitor.getHeight() / 4f, 0.5f, 0.5f);
        }


        spriteBasketBack.setSize((rightBody.getPosition().x - leftBody.getPosition().x) + 2 * 0.5f, 1.5f);
        spriteBasketBack.setOriginCenter();
        batch.draw(spriteBasketBack, leftBody.getPosition().x - 0.5f, 5f,
                (rightBody.getPosition().x - leftBody.getPosition().x) + 2 * 0.5f, 1.5f);

        if (!topOfBasket) {
            spriteBasketRim.setSize(rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);
            spriteBasketRim.setOriginCenter();
            batch.draw(spriteBasketRim, leftBody.getPosition().x, leftBody.getPosition().y + 3 * RIM_RADIOS - 2 * BALL_RADIOS,
                    rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);
        }


        if (shoot && ballBody.getLinearVelocity().y == 0 ||
                (ballBody.getPosition().x + BALL_RADIOS < 0 || ballBody.getPosition().x - BALL_RADIOS > cam.viewportWidth)) {

            if (win) {
                ballStored++;
                win = false;
            } else {
                //lose
            }

            if (round == 1 && ballStored < 3) {
                resetGame();
            } else if (round == 1 && ballStored > 3) {
                ballRemain = ballStored;
                ballStored = 0;
                round++;

                leftInJar = null;

                moving = true;

                topDownContainer = null;

                //reset game after animation done();
            } else if (round == 2) {
                if (ballRemain > 0) {
                    resetGame();
                } else {
                    if (ballStored > 0) {

                        ballRemain = ballStored;
                        ballStored = 0;
                        round++;

                        leftInJar = null;

                        moving = true;

                        topDownContainer = null;

                        //reset game after animation done();

                    } else {
                        //game over
                        gameOver = true;
                    }
                }
            } else {

                if (ballRemain > 0) {
                    resetGame();
                } else {
                    if (ballStored > 0) {

                        ballRemain = ballStored;
                        ballStored = 0;
                        round++;

                        leftInJar = null;
                        moving = true;
                        topDownContainer = null;

                        float left = 0.5f + BALL_RADIOS;
                        float right = cam.viewportWidth - 0.5f - BALL_RADIOS;

                        if (round > 2) {
                            if (round % 2 == 0) {
                                xpos = left;
                            } else {
                                xpos = right;
                            }
                        }

                        //reset game after animation done

                    } else {
                        //game over
                        gameOver = true;
                    }
                }
            }
        }

        Gdx.app.log("Variables", "remain: " + ballRemain + ", stored: " + ballStored + ", round:" + round);

        if (round > 1) {
            if (ballStored > 0) {
                if (ballStored > 3) {
                    currentJar = spriteBallJar[3];
                } else {
                    currentJar = spriteBallJar[ballStored];
                }
            } else {
                currentJar = spriteBallJar[0];
                if (leftInJar == null && !moving) {
                    leftInJar = Tween.from(currentJar, SpriteAccessor.TYPE_X, 1f)
                            .target(0f)
                            .ease(TweenEquations.easeNone)
                            .start(manager);
                }
            }

        } else {
            if (ballStored > 0) {
                if (ballStored > 3) {
                    currentJar = spriteBallJar[3];
                } else {
                    currentJar = spriteBallJar[ballStored];
                    if (leftInJar == null && !moving) {
                        leftInJar = Tween.from(currentJar, SpriteAccessor.TYPE_X, 1f)
                                .target(0f)
                                .ease(TweenEquations.easeNone)
                                .start(manager);
                    }

                }
            }
        }

        if (currentJar != null && !moving) {
            batch.draw(currentJar, currentJar.getX(), currentJar.getY(), 0.5f, 1f);
        }

        if (ballRemain >= 0 && ballRemain < 4) {
            currentContainer = spriteBallContainer[ballRemain];
            if (moving && topDownContainer == null) {
                topDownContainer = Tween.from(currentContainer, SpriteAccessor.TYPE_Y, 1f)
                        .target(3.4f)
                        .ease(TweenEquations.easeNone)
                        .setCallbackTriggers(TweenCallback.COMPLETE)
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int type, BaseTween<?> source) {
                                leftInJar = null;
                                ballRemain--;
                                resetGame();
                                moving = false;
                            }
                        })
                        .start(manager);
            }
        } else {
            currentContainer = spriteBallContainer[0];

            if (moving && topDownContainer == null) {
                topDownContainer = Tween.from(currentContainer, SpriteAccessor.TYPE_Y, 2f)
                        .target(3.4f)
                        .ease(TweenEquations.easeNone)
                        .setCallbackTriggers(TweenCallback.COMPLETE)
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int type, BaseTween<?> source) {
                                leftInJar = null;
                                ballRemain--;
                                resetGame();
                                moving = false;
                            }
                        })
                        .start(manager);
            }

        }

        if (currentContainer != null && round > 1) {
            batch.draw(currentContainer, currentContainer.getX(), currentContainer.getY(), 0.5f, 1f);
        }

        if (!moving) {
            spriteBall.setSize(2 * r, 2 * r);
            spriteBall.setOriginCenter();
            batch.draw(spriteBall, ballBody.getPosition().x - r, ballBody.getPosition().y - r,
                    r * 2, 2 * r);
        }

        if (data.isHint()) {
            spriteHintCircle.setSize(3f * r, 3f * r);
            spriteHintCircle.setOriginCenter();
            batch.draw(spriteHintCircle, ballBody.getPosition().x - r * 1.5f,
                    ballBody.getPosition().y - r * 1.5f, r * 3, 3 * r);

//            spriteHintArrow.setSize(2f, 2f);
//            spriteHintArrow.setOriginCenter();
//            batch.draw(spriteHintArrow, cam.viewportWidth / 2f, ballBody.getPosition().y,
//                    2f, 2f);
        }

        if (drawGem) {
            batch.draw(spriteGem, spriteGem.getX(),
                    spriteGem.getY(), 0.5f, 0.5f);
        }

        if (topOfBasket) {
            spriteBasketRim.setOriginCenter();
            batch.draw(spriteBasketRim, leftBody.getPosition().x, leftBody.getPosition().y + (3 * RIM_RADIOS) - 2 * BALL_RADIOS,
                    rightBody.getPosition().x - leftBody.getPosition().x, 2 * BALL_RADIOS);
        }

        if (gameOver) {
            gameOver();
        }

        batch.end();

//        debugRender.render(world, cam.combined);
        world.step(1 / 60f, 6, 2);

    }

    private void gameOver() {
        batch.setProjectionMatrix(uiCam.combined);
        batch.draw(spriteGameOver, 0, 0, uiCam.viewportWidth, uiCam.viewportHeight);
        batch.setProjectionMatrix(cam.combined);
    }

    private void resetGame() {
        topOfBasket = false;
        win = false;
        groundFixTop.setSensor(true);
        ballBody.getWorld().destroyBody(ballBody);

        leftBody.getFixtureList().get(0).setSensor(true);
        rightBody.getFixtureList().get(0).setSensor(true);

        leftLine.getFixtureList().get(0).setSensor(true);
        rightLine.getFixtureList().get(0).setSensor(true);

        if (!moving) {
            ballRemain--;
        }

        createBall();

    }

    public void dispose() {

        croowedSound.dispose();
        dropSound1.dispose();
        shootSound.dispose();

        font12.dispose();

        texture.dispose();
        batch.dispose();
        spriteBasketBack.getTexture().dispose();
        spriteBasketBack.getTexture().dispose();
        spriteBall.getTexture().dispose();
//        debugRender.dispose();
        world.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {

        if (data.isHint()) {
            data.setHint(false);
            flashHintAnim.pause();
            flashHintAnim = null;
        }

        if (gameOver) {

            gameOver = false;
            round = 1;
            score = 0;
            xpos = cam.viewportWidth / 2f;
            retry++;
            data.setBack(false);

            resetGame();
        }
        point = new Vector3();
        point.set(x, y, 0); // Translate to world coordinates.
        cam.unproject(point);
        wasTouched = ballBody.getFixtureList().first().testPoint(point.x, point.y);
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {

        float angle = -(float) Math.toDegrees(Math.atan2(velocityY, velocityX));

        if (angle > 90) {
            angle = 90 + ((angle - 90) / 5);
        } else if (angle < 90) {
            angle = 90 - ((90 - angle) / 5);
        }

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {

            } else {

            }
        } else {
            if (velocityY > 0) {

            } else {
                if (wasTouched && !gameOver && !data.isHint()) {
                    if (ballBody.getLinearVelocity().y == 0) {

                        shoot = true;
//                        ballRemain--;

                        float speed = new Vector2(ballBody.getPosition().x, ballBody.getPosition().y)
                                .dst(new Vector2(point2.x, point2.y));

                        shootSound.play();

                        Vector2 initialVelocity = new Vector2(Math.min(8f, Math.max(7.5f, speed * 2)),
                                Math.min(8f, Math.max(7.5f, speed * 2)));
                        initialVelocity.rotate(angle - 45);

                        ballBody.setLinearVelocity(initialVelocity.x, initialVelocity.y);
                        ballBody.getFixtureList().get(0).getShape().setRadius(0.3f);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        point2 = new Vector3();
        point2.set(x, y, 0);
        cam.unproject(point2);
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public void beginContact(Contact contact) {
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getUserData() == "BASKET") {
            if (topOfBasket) {
                win = true;
                croowedSound.play();
                score++;

                drawEmoji = true;

                number = getRandomNumberInRange(0, 4);

                winEmoji[number].setX(spriteTopMonitor.getX() + 1f);

                emoJiAnimatino = Tween.to(winEmoji[number], SpriteAccessor.TYPE_X, 1.0f)
                        .target(spriteTopMonitor.getWidth() - 0.5f)
                        .ease(TweenEquations.easeInOutCubic)
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int type, BaseTween<?> source) {
                                drawEmoji = false;
                            }
                        })
                        .setCallbackTriggers(TweenCallback.COMPLETE)
                        .start(manager);

                if (round == 2 && data.isBack()) {
                    drawGem = true;
                    spriteGem.setPosition(cam.viewportWidth / 2f, cam.viewportHeight / 2f);
                    gemAnimation = Tween.to(spriteGem, SpriteAccessor.TYPE_XY, 2.0f)
                            .target(cam.viewportWidth, cam.viewportHeight)
                            .ease(TweenEquations.easeInOutCubic)
                            .setCallback(new TweenCallback() {
                                @Override
                                public void onEvent(int type, BaseTween<?> source) {
                                    drawGem = false;
                                }
                            })
                            .setCallbackTriggers(TweenCallback.COMPLETE)
                            .start(manager);
                }
            }

        } else if (B.getBody().getUserData() == "BASKET") {
            if (topOfBasket) {

                win = true;
                croowedSound.play();
                score++;

                drawEmoji = true;

                number = getRandomNumberInRange(0, 4);

                winEmoji[number].setX(spriteTopMonitor.getX() + 1f);

                emoJiAnimatino = Tween.to(winEmoji[number], SpriteAccessor.TYPE_X, 1.0f)
                        .target(spriteTopMonitor.getWidth() - 0.5f)
                        .ease(TweenEquations.easeInOutCubic)
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int type, BaseTween<?> source) {
                                drawEmoji = false;
                            }
                        })
                        .setCallbackTriggers(TweenCallback.COMPLETE)
                        .start(manager);

                if (round == 2 && retry == 0) {
                    drawGem = true;
                    spriteGem.setPosition(cam.viewportWidth / 2f, cam.viewportHeight / 2f);
                    gemAnimation = Tween.to(spriteGem, SpriteAccessor.TYPE_XY, 2.0f)
                            .target(cam.viewportWidth, cam.viewportHeight)
                            .ease(TweenEquations.easeInOutCubic)
                            .setCallback(new TweenCallback() {
                                @Override
                                public void onEvent(int type, BaseTween<?> source) {
                                    drawGem = false;
                                }
                            })
                            .setCallbackTriggers(TweenCallback.COMPLETE)
                            .start(manager);
                }

            }

        } else if (!A.isSensor() && !B.isSensor()) {
            Gdx.app.log("Hit", "A: " + A.getBody().getUserData() + " B: " + B.getBody().getUserData());
            if (A.getBody().getUserData() != null) {
                if (A.getBody().getUserData() == "beep") {
                    dropSound1.play();
                }
            } else if (B.getBody().getUserData() != null) {
                if (B.getBody().getUserData() == "beep") {
                    dropSound1.play();
                }
            }
        }

    }

    @Override
    public void endContact(Contact contact) {
        Fixture A = contact.getFixtureA();
        Fixture B = contact.getFixtureB();

        if (A.getBody().getUserData() == "basketline") {
            if (!topOfBasket) {
                leftBody.getFixtureList().get(0).setSensor(false);
                rightBody.getFixtureList().get(0).setSensor(false);

                leftLine.getFixtureList().get(0).setSensor(false);
                rightLine.getFixtureList().get(0).setSensor(false);

                topOfBasket = true;
            } else {
                groundFixTop.setSensor(false);
            }
        } else if (B.getBody().getUserData() == "basketline") {
            if (!topOfBasket) {
                leftBody.getFixtureList().get(0).setSensor(false);
                rightBody.getFixtureList().get(0).setSensor(false);
                leftLine.getFixtureList().get(0).setSensor(false);
                rightLine.getFixtureList().get(0).setSensor(false);
                topOfBasket = true;
            } else {
                groundFixTop.setSensor(false);
            }
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}