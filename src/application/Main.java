package application;
	
import java.util.Vector;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import sprite.Car;
import sprite.FinishLine;
import sprite.Road;
import sprite.Sprite;
import sprite.SpriteHandler;
import sprite.Wall;
import utils.RestrictiveTextField;


public class Main extends Application 
{
	private static final int LONG_GAME_LENGTH = -15;
	private static final int SHORT_GAME_LENGTH = -8;
	private static final double MOVEMENT_AMOUNT = 4.5;
	public static final int TOP_BUFFER = 200;
	public static final String CAR_YELLOW_HTML = "#FFC601";
	public static final String CAR_BLUE_HTML = "#2490FB";
	public static final Color CAR_YELLOW = Color.web(CAR_YELLOW_HTML);
	public static final Color CAR_BLUE = Color.web(CAR_BLUE_HTML);
	private static final double TIME_GAP = 0.1;
	
	private AnimationTimer animTimer;
	private boolean endOfTrack = false;
	private boolean longGame = false;
	private static boolean gameWon = false;
	private boolean testMode = false;
	private double time = 0;
	private Timeline timer = null;
	private GridPane topBar = null;
	private Vector<Car> cars = null;
	private double trackDispTwoPrev = 0;
	
	private static boolean singlePlayer = true;
	
	private Player playerOne = null;
	private Player playerTwo = null;
	
	private double trackDispLargest = 0;
	
	private static int roadNumberCoefficient = 1; //1 builds zero roads. Decrease to build more rows (see createCarAndRoads())
	private static double trackDistance = 0;
	
	public void start(Stage primaryStage) 
	{
		try 
		{			
			primaryStage.setTitle("DragFx");
			
			Scene initialScene = createInitialScene(primaryStage);
			initialScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(initialScene);
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	private Scene createInitialScene(Stage primaryStage)
	{
		BorderPane btnPane = new BorderPane();
		btnPane.setPadding(new Insets(25, 25, 25, 25));
		
		Button btnSinglePlayer = new Button("Single player game");
		btnSinglePlayer.setDefaultButton(true);
		HBox hbBtnSingle = new HBox(10);
		hbBtnSingle.getChildren().add(btnSinglePlayer);
		
		Button btnMultiPlayer = new Button("Multiplayer Game");
		btnMultiPlayer.setDefaultButton(true);
		HBox hbBtnMulti = new HBox(10);
		hbBtnMulti.getChildren().add(btnMultiPlayer);
		
		Button btnLeaderboard = new Button("Leaderboard");
		btnLeaderboard.setDefaultButton(true);
		HBox hbBtnLeaderboard = new HBox(10);
		hbBtnLeaderboard.getChildren().add(btnLeaderboard);
		
		setupInitialButtonActions(btnSinglePlayer, btnMultiPlayer, btnLeaderboard, primaryStage);
		
		btnPane.setTop(btnSinglePlayer);
		btnPane.setCenter(btnMultiPlayer);
		btnPane.setBottom(btnLeaderboard);
		setCentralAlignment(btnSinglePlayer, btnLeaderboard);
		
		Scene initialScene = new Scene(btnPane, 300, 200);
		return initialScene;
	}

	private void setCentralAlignment(Button btnSinglePlayer,
			Button btnLeaderboard)
	{
		BorderPane.setAlignment(btnSinglePlayer, Pos.CENTER);
		BorderPane.setAlignment(btnLeaderboard, Pos.CENTER);
	}

	private void setupInitialButtonActions(Button btnSinglePlayer, Button btnMultiPlayer, Button btnLeaderboard, Stage primaryStage)
	{
		btnSinglePlayer.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{			
				selectOptions(primaryStage);
			}
		});
		
		btnMultiPlayer.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{		
				singlePlayer = false;
				selectOptions(primaryStage);
			}
		});
	}

	private void selectOptions(Stage primaryStage)
	{
		GridPane entryGrid = new GridPane();
		entryGrid.setAlignment(Pos.CENTER);
		entryGrid.setHgap(10);
		entryGrid.setVgap(10);
		entryGrid.setPadding(new Insets(25, 25, 25, 25));
		
		initialiseEntryPane(entryGrid, primaryStage);
		
		Scene entryScene = new Scene(entryGrid, 300, 275);
		entryScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		primaryStage.setScene(entryScene);
		primaryStage.show();
	}

	private void startGame(Stage primaryStage)
	{
		Vector<Player> players = new Vector<Player>();
		players.add(playerOne);
		if (!singlePlayer)
		{
			players.add(playerTwo);
		}		
				
		setRoadNumberCoefficient();
		
		createSprites();
		
		topBar = new GridPane(); 
		topBar.setId("top-bar");
		topBar.setPadding(new Insets(5, 5, 5, 5));
		topBar.setPrefSize(Car.GAME_CANVAS_WIDTH, 50);
		
		Label timerText = new Label();
		timerText.setId("timer-text");
		topBar.add(timerText, 0, 0);
						
		HBox canvasHBox = new HBox();
		Vector<Group> progressBars = new Vector<Group>();
		for (int i=0; i<cars.size(); i++)
		{
			Car car = cars.elementAt(i);
			Group progressBar = car.initProgressBar();
			progressBars.add(progressBar);
			initProgressBar(car);
			canvasHBox.getChildren().add(car.getGameCanvas());
			drawGame(car);
		}	
		
		HBox hBox = new HBox();
		for (int i=0; i<progressBars.size(); i++)
		{
			Group progressBar = progressBars.elementAt(i);
			hBox.getChildren().add(progressBar);
		}
		
		Scene gameScene = new Scene(new VBox(topBar, new HBox(canvasHBox, hBox)));	
		gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				
		gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() 
		{
			public void handle(KeyEvent event) 
			{
				handleKeyPressed(event);						
			}				
		});
		
		gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() 
		{
			public void handle(KeyEvent event) 
			{
				handleKeyReleased(event);
			}
		});
		
		primaryStage.setScene(gameScene);
		primaryStage.show();	
		
		timer = new Timeline(
			new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
		          @Override public void handle(ActionEvent actionEvent) {
		        	  	timerText.setText("" + time);
		        	  	time = time + TIME_GAP;
		            }
		          }),
		    new KeyFrame(Duration.seconds(TIME_GAP))
		);
		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();
		
		animTimer = new AnimationTimer() 
		{
			public void handle(long arg0) 
			{
				/*if (!singlePlayer)
				{
					Car carOne = cars.elementAt(0);
					Car carTwo = cars.elementAt(1);
					
					double trackDispOne = carOne.getTrackDisposition();
					double trackDispTwo = carTwo.getTrackDisposition();

					if (trackDispTwo > 0 && trackDispTwo > trackDispTwoPrev + 1)
					{
						System.out.println("trackDispOne = " + trackDispOne);
						System.out.println("trackDispTwo = " + trackDispTwo);
						System.out.println("sprOne = " + carOne.getSpriteModifier());
						System.out.println("carMOne = " + carOne.getCarModifier());
						System.out.println("sprTwo = " + carTwo.getSpriteModifier());
						System.out.println("carMTwo = " + carTwo.getCarModifier());
						System.out.println("carOneY = " + carOne.getPosY());
						System.out.println("carTwoY = " + carTwo.getPosY());
						
						trackDispTwoPrev = trackDispTwo;
					}
					
					if (trackDispOne > trackDispTwo)
					{
						modifyTrackPositions(carOne, trackDispOne, carTwo, trackDispTwo);
						trackDispLargest = trackDispOne;
					}
					else if (trackDispTwo > trackDispOne)
					{
						modifyTrackPositions(carTwo, trackDispTwo, carOne, trackDispOne);
						trackDispLargest = trackDispTwo;
					}
				}*/
				update(gameScene);
			}			
		};
		
		animTimer.start();
	}

	/*private void modifyTrackPositions(Car carWithGreaterTrackDisposition, double trackDispGreater, 
							Car carWithLessTrackDisposition, double trackDispLesser)
	{
		carWithGreaterTrackDisposition.setCarModifier(trackDispGreater - trackDispLesser);
		carWithGreaterTrackDisposition.setSpriteModifier(0);
		carWithLessTrackDisposition.setCarModifier(-(trackDispGreater - trackDispLesser));
		carWithLessTrackDisposition.setSpriteModifier(-(trackDispGreater - trackDispLesser));
	}*/

	private void initProgressBar(Car car)
	{
		double carY = car.getPosY();
		trackDistance = carY-car.getSprites().getTrackEnd();
		car.updateProgressBar();
	}

	private void update(Scene gameScene)
	{
		if (!gameWon)
		{
			updatePosition();
			for (int i=0; i<cars.size(); i++)
			{
				Car car = cars.elementAt(i);
				if (car.getCarCloseToTop() && !endOfTrack)
				{
					scrollScreen(car, car.getSprites());
					break;
				}
			}
		}
		else 
		{
			gameScene.setOnKeyPressed(null);
			gameScene.setOnKeyReleased(null);
			
			animTimer.stop();
			timer.stop();					
		}
	}	
	
	private void setRoadNumberCoefficient()
	{
		if (testMode)
		{
			roadNumberCoefficient = -2;
		}
		else if (longGame)
		{
			roadNumberCoefficient = LONG_GAME_LENGTH;
		}
		else
		{
			roadNumberCoefficient = SHORT_GAME_LENGTH;
		}
	}

	private void initialiseEntryPane(GridPane entryGrid, Stage primaryStage)
	{
		Text scenetitle = new Text("DragFx");
		scenetitle.setId("dragfx-text");
		entryGrid.add(scenetitle, 0, 0, 2, 1);
		
		if (singlePlayer)
		{
			setupSinglePlayerGame(entryGrid, primaryStage);
		}
		else
		{
			setupMultiPlayerGame(entryGrid, primaryStage);
		}
	}

	private void setupMultiPlayerGame(GridPane entryGrid, Stage primaryStage)
	{
		Label nameOne = new Label("Player one name:");
		entryGrid.add(nameOne, 0, 1);
		
		RestrictiveTextField nameOneTextField = new RestrictiveTextField();
		nameOneTextField.setMaxLength(12);
		entryGrid.add(nameOneTextField, 1, 1);
		
		Label nameTwo = new Label("Player two name:");
		entryGrid.add(nameTwo, 0, 2);
		
		RestrictiveTextField nameTwoTextField = new RestrictiveTextField();
		nameTwoTextField.setMaxLength(12);
		entryGrid.add(nameTwoTextField, 1, 2);
		
		Button btnLong = new Button("Long game");
		btnLong.setDefaultButton(true);
		HBox hbBtnLong = new HBox(10);
		hbBtnLong.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtnLong.getChildren().add(btnLong);
		entryGrid.add(hbBtnLong, 1, 3);
		
		Button btnShort = new Button("Short game");
		btnShort.setDefaultButton(true);
		HBox hbBtnShort = new HBox(10);
		hbBtnShort.setAlignment(Pos.BOTTOM_LEFT);
		hbBtnShort.getChildren().add(btnShort);
		entryGrid.add(hbBtnShort, 0, 3);
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 0, 4, 2, 1);
		
        setMultiPlayerButtonActions(primaryStage, nameOneTextField, nameTwoTextField, btnLong, btnShort, errorAction);
	}

	private void setMultiPlayerButtonActions(Stage primaryStage, RestrictiveTextField nameOneTextField, RestrictiveTextField nameTwoTextField, Button btnLong, 
								Button btnShort, Text errorAction)
	{
		btnLong.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{			
				longGame = true;								
				initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction);
			}
		});
		
		btnShort.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{
				initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction);
			}
		});
	}
	
	private void setupSinglePlayerGame(GridPane entryGrid, Stage primaryStage)
	{
		Label name = new Label("Name:");
		entryGrid.add(name, 0, 1);

		RestrictiveTextField nameTextField = new RestrictiveTextField();
		nameTextField.setMaxLength(12);
		entryGrid.add(nameTextField, 1, 1);
		
		CheckBox cBox = new CheckBox("Use WAD controls");
		entryGrid.add(cBox, 0, 4, 2, 1);
		
		ComboBox<Color> cmb = addCarColorComboBox(entryGrid);
		
		Button btnLong = new Button("Long game");
		btnLong.setDefaultButton(true);
		HBox hbBtnLong = new HBox(10);
		hbBtnLong.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtnLong.getChildren().add(btnLong);
		entryGrid.add(hbBtnLong, 1, 6);
		
		Button btnShort = new Button("Short game");
		btnShort.setDefaultButton(true);
		HBox hbBtnShort = new HBox(10);
		hbBtnShort.setAlignment(Pos.BOTTOM_LEFT);
		hbBtnShort.getChildren().add(btnShort);
		entryGrid.add(hbBtnShort, 0, 6);
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 0, 7, 2, 1);
		
		setSinglePlayerButtonActions(primaryStage, nameTextField, btnLong, btnShort, cBox, errorAction, cmb);
	}

	private ComboBox<Color> addCarColorComboBox(GridPane entryGrid)
	{		
		ComboBox<Color> cmb = new ComboBox<Color>();
        cmb.getItems().addAll(CAR_YELLOW, CAR_BLUE);
        
        cmb.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
        	public ListCell<Color> call(ListView<Color> p) 
        	{
        		return new ListCell<Color>() {
        			private final Rectangle rectangle;
        			{ 
        				setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
        				rectangle = new Rectangle(10, 10);
        			}

        			protected void updateItem(Color item, boolean empty) 
        			{
        				super.updateItem(item, empty);

        				if (item == null || empty) 
        				{
        					setGraphic(null);
        				} 
        				else 
        				{
        					rectangle.setFill(item);
        					setGraphic(rectangle);
        				}
        			}
        		};
        	}
        });    
        
        //render selected colour in combobox
        cmb.setButtonCell(cmb.getCellFactory().call(null));
        
        Label lbl = new Label("Choose car colour: ");
        
        entryGrid.add(new HBox(lbl, cmb), 0, 5, 2, 1);
        
        return cmb;
	}

	private void setSinglePlayerButtonActions(Stage primaryStage, RestrictiveTextField nameTextField, Button btnLong, Button btnShort, CheckBox cBox, 
			final Text errorAction, ComboBox<Color> cmb)
	{
		btnLong.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{			
				longGame = true;								
				initialiseSinglePlayerGame(primaryStage, nameTextField, cBox, errorAction, cmb);
			}
		});
		
		btnShort.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{
				initialiseSinglePlayerGame(primaryStage, nameTextField, cBox, errorAction, cmb);
			}
		});
	}
	
	private void initialiseSinglePlayerGame(Stage primaryStage, RestrictiveTextField nameTextField, CheckBox cBox, final Text errorAction, ComboBox<Color> cmb)
	{
		errorAction.setText("");
		errorAction.setFill(Color.FIREBRICK);
		
		playerOne = new Player();
		
		String name = nameTextField.getText();
		if (!checkNameEntered(errorAction, name))
		{
			return;
		}
		
		playerOne.setUseAlternateControls(cBox.isSelected());
		playerOne.setCarColor(cmb.getValue());
		if (playerOne.getCarColor() == null)
		{
			errorAction.setText("You must select a colour for you car!");
			return;
		}
		
		if (testMode)
		{
			playerOne.setName("Test subject");
		}
		else
		{
			playerOne.setName(name);
		}
		
		startGame(primaryStage);
	}	

	private void initialiseMultiPlayerGame(Stage primaryStage, RestrictiveTextField nameOneTextField, RestrictiveTextField nameTwoTextField, Text errorAction)
	{
		errorAction.setText("");
		errorAction.setFill(Color.FIREBRICK);
		
		playerOne = new Player();
		playerTwo = new Player();
		
		String nameOne = nameOneTextField.getText();
		String nameTwo = nameTwoTextField.getText();
		
		if (nameOne.isEmpty() || nameTwo.isEmpty())
		{
			return;
		}
		
		playerOne.setUseAlternateControls(true);
		playerOne.setCarColor(CAR_YELLOW);
		playerTwo.setCarColor(CAR_BLUE);
		
		playerOne.setName(nameOne);
		playerTwo.setName(nameTwo);
		
		startGame(primaryStage);
	}
	
	private boolean checkNameEntered(final Text errorAction, String name)
	{
		if (name.isEmpty())
		{
			errorAction.setText("You must enter a name!");
			return false;
		}
		else if (name.equals("t"))
		{
			testMode = true;
		}
		
		return true;
	}

	private void handleKeyReleased(KeyEvent event)
	{
		for (int i=0; i<cars.size(); i++)
		{
			Car car = cars.elementAt(i);
			Player player = car.getPlayer();
			boolean useAlternateControls = player.getUseAlternateControls();
			if (event.getCode() == KeyCode.UP && !useAlternateControls
					|| event.getCode() == KeyCode.W && useAlternateControls)  
			{
				car.setVerticalEnabled(false);
			}
			else if ((event.getCode() == KeyCode.RIGHT && !useAlternateControls || event.getCode() == KeyCode.D && useAlternateControls)
					|| (event.getCode() == KeyCode.LEFT && !useAlternateControls || event.getCode() == KeyCode.A && useAlternateControls))
			{
				car.setRotationEnabled(false);
				car.setRotMove(0);
			}
		}
	}
	
	private void handleKeyPressed(KeyEvent event)
	{
		for (int i=0; i<cars.size(); i++)
		{
			Car car = cars.elementAt(i);
			Player player = car.getPlayer();
			boolean useAlternateControls = player.getUseAlternateControls();
			if (event.getCode() == KeyCode.UP && !useAlternateControls
					|| event.getCode() == KeyCode.W && useAlternateControls) 
			{
				car.setVerticalEnabled(true);
			}
			else if (event.getCode() == KeyCode.RIGHT && !useAlternateControls
					|| event.getCode() == KeyCode.D && useAlternateControls)
			{
				car.setRotationEnabled(true);
				car.setRotMove(5);						
			}
			else if (event.getCode() == KeyCode.LEFT && !useAlternateControls
					|| event.getCode() == KeyCode.A && useAlternateControls)
			{
				car.setRotationEnabled(true);
				car.setRotMove(-5);
			}
		}
	}

	private void createSprites()
	{
		SpriteHandler sprites = new SpriteHandler(roadNumberCoefficient);
		SpriteHandler spritesCopy = null;
		cars = new Vector<Car>();
		if (singlePlayer)
		{
			Image carImage = null;
			if (playerOne.getCarColor().equals(CAR_YELLOW))
			{
				carImage = new Image(this.getClass().getResource("CarPixlr.png").toString());
			}
			else
			{
				carImage = new Image(this.getClass().getResource("CarPixlrBlue.png").toString());
			}			
			Car car = new Car(carImage, 147.5, 650, 0, playerOne);			
			sprites.add(car);
			cars.add(car);
		}
		else
		{
			Image carOneImage = new Image(this.getClass().getResource("CarPixlr.png").toString());
			Car carOne = new Car(carOneImage, 147.5, 650, 0, playerOne);
			
			sprites.add(carOne);
			cars.add(carOne);
		}
		
		Image roadImage = new Image(this.getClass().getResource("Road.png").toString());
		Image wallImage = new Image(this.getClass().getResource("Wall.png").toString());
		Image finishLineImage = new Image(this.getClass().getResource("FinishLine.png").toString());
				
		//Remember that anything that you want to add here also has to be added to the method that draws the components
		addRoads(roadImage, sprites);		
		addWalls(wallImage, sprites);		
		checkWallsArePlacedCorrectly(sprites);
		addFinishLine(finishLineImage, sprites);

		Car car = cars.elementAt(0);
		car.setSprites(sprites);
		
		if (!singlePlayer)
		{
			spritesCopy = createSpritesVectorCopy(sprites);
			Car carTwo = (Car) spritesCopy.elementAt(0);
			cars.add(carTwo);
			carTwo.setSprites(spritesCopy);
		}				
	}

	private SpriteHandler createSpritesVectorCopy(SpriteHandler sprites)
	{
		SpriteHandler spritesCopy = new SpriteHandler(roadNumberCoefficient);
		for (int i=0; i<sprites.size(); i++)
		{
			Sprite sprite = sprites.elementAt(i);
			if (sprite instanceof Car)
			{				
				Car car = new Car(new Image(this.getClass().getResource("CarPixlrBlue.png").toString()), 
								sprite.getPosX(), sprite.getPosY(), ((Car) sprite).getRotation(), playerTwo);
				spritesCopy.add(car);
			}
			else if (sprite instanceof Road)
			{
				Road road = new Road(sprite.getPosX(), sprite.getPosY(), sprite.getImage());
				spritesCopy.add(road);
			}
			else if (sprite instanceof Wall)
			{
				Wall wall = new Wall(sprite.getPosX(), sprite.getPosY(), sprite.getImage());
				spritesCopy.add(wall);
			}
			else if (sprite instanceof FinishLine)
			{
				FinishLine fl = new FinishLine(sprite.getPosX(), sprite.getPosY(), sprite.getImage());
				spritesCopy.add(fl);
			}
		}
		return spritesCopy;
	}

	private void addFinishLine(Image finishLineImage, SpriteHandler sprites)
	{
		FinishLine finishLine = new FinishLine(0, (roadNumberCoefficient+1)*Road.HEIGHT + Car.HEIGHT, finishLineImage);
		sprites.add(finishLine);
	}

	private void checkWallsArePlacedCorrectly(SpriteHandler sprites)
	{
		boolean wallsInValidPositions = false;
		while (!wallsInValidPositions)
		{
			wallsInValidPositions = sprites.checkWallsArePlacedCorrectly();
		}
	}

	private void addRoads(Image roadImage, SpriteHandler sprites)
	{
		for (int i=1; i>roadNumberCoefficient; i--)
		{
			Road road = new Road(0, i*Road.HEIGHT, roadImage);
			sprites.add(road);
		}
	}

	private void addWalls(Image wallImage, SpriteHandler sprites)
	{
		int numberOfWalls = (Math.abs(roadNumberCoefficient) + 2)*2;
		
		for (int i=0; i<numberOfWalls; i++)
		{
			double rndY = Wall.getRandomYCoordinate(roadNumberCoefficient + 1);
			double rndX = Wall.getRandomXCoordinate();
						
			Wall wall = new Wall(rndX, rndY, wallImage);
			sprites.add(wall);
		}
	}
	
	private void scrollScreen(Car car, SpriteHandler sprites)
	{
		sprites.scrollSprites(car);
		car.setCarCloseToTop(false);
	}
	
	private void drawSprites(GraphicsContext gc, Car car)
	{	
		boolean reachedEndOfTrack = true;
		SpriteHandler sprites = car.getSprites();
		int size = sprites.size();
		double spriteMod = car.getSpriteModifier();
		for (int i=0; i<size; i++)
		{
			Sprite sprite = sprites.elementAt(i);
			if (sprite instanceof Road)
			{
				Road road = (Road) sprite;
				gc.drawImage(road.getImage(), road.getPosX(), road.getPosY() + spriteMod);				
				if (road.getPosY() + spriteMod < 0)
				{
					reachedEndOfTrack = false;
				}
			}
		}
		for (int i=0; i<size; i++)
		{
			Sprite sprite = sprites.elementAt(i);
			if (!(sprite instanceof Road)
			  && !(sprite instanceof Car))
			{
				gc.drawImage(sprite.getImage(), sprite.getPosX(), sprite.getPosY() + spriteMod);
			}
		}
		
		if (reachedEndOfTrack)
		{
			endOfTrack = true;
		}
	}

	private void drawGame(Car car)
	{
		Canvas gameCanvas = car.getGameCanvas();
		GraphicsContext gc = gameCanvas.getGraphicsContext2D();
		
		double width = gameCanvas.getWidth();
		double height = gameCanvas.getHeight();
		
		gc.clearRect(0, 0, width, height);
		drawSprites(gc, car);
		
		render(gc, car);
	}

	private void render(GraphicsContext gc, Car car)
	{
		if (gameWon)
		{
			for (int i=0; i<cars.size(); i++)
			{
				Car checkCar = cars.elementAt(i);
				if (checkCar.getWinner())
				{
					Label winnerText = new Label(checkCar.getPlayer().getName() + " is the winner!");
					winnerText.setId("winner-text");	
					topBar.add(winnerText, 0, 1, 2, 1);
				}
			}
		}

		double x = car.getPosX() + car.getXMove();
		double y = car.getPosY() + car.getYMove();
		drawCarWithRotation(gc, x, y, car);
	}

	private void drawCarWithRotation(GraphicsContext gc, double x, double y, Car car)
	{
		gc.save();
		gc.translate(x + (Car.WIDTH)/2, y + (Car.HEIGHT)/2);
		gc.rotate(car.getRotation());

		gc.drawImage(car.getImage(), -(Car.WIDTH)/2, -(Car.HEIGHT)/2);

		gc.restore();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	private void updatePosition()
	{
		for (int i=0; i<cars.size(); i++)
		{
			Car car = cars.elementAt(i);
			boolean verticalEnabled = car.getVerticalEnabled();
			boolean rotationEnabled = car.getRotationEnabled();
			if (verticalEnabled)
			{
				updateVerticalPosition(true, car);
			}

			if (rotationEnabled)
			{
				updateRotation(car);
			}

			double xMove = car.getXMove();
			double yMove = car.getYMove();
			boolean inMotion = yMove > 0 || yMove < 0 || xMove < 0 || xMove > 0;
			if (!verticalEnabled && inMotion)
			{
				yMove = calculateYMovement(car);
				xMove = calculateXMovement(car);

				car.setXMove(xMove);
				car.setYMove(yMove);

				updateVerticalPosition(false, car);
			}
			
			drawGame(car);
		}		
	}

	private double calculateYMovement(Car car)
	{
		double newYMove;
		double yMove = car.getYMove();
		if (yMove > 0)
		{
			newYMove = yMove-0.25;
			if (newYMove > 0)
			{
				yMove = newYMove;
			}
			else
			{
				yMove = 0;
			}
		}
		else if (yMove < 0)
		{
			newYMove = yMove+0.25;
			if (newYMove < 0)
			{
				yMove = newYMove;
			}
			else
			{
				yMove = 0;
			}
		}
		
		return yMove;
	}
	
	private double calculateXMovement(Car car)
	{
		double newXMove;
		double xMove = car.getXMove();
		if (xMove > 0)
		{
			newXMove = xMove-0.25;
			if (newXMove > 0)
			{
				xMove = newXMove;
			}
			else
			{
				xMove = 0;
			}
		}
		else if (xMove < 0)
		{
			newXMove = xMove+0.25;
			if (newXMove < 0)
			{
				xMove = newXMove;
			}
			else
			{
				xMove = 0;
			}
		}
		
		return xMove;
	}

	private void updateRotation(Car car)
	{
		double oldRot = car.getRotation();
		double newRot = oldRot + car.getRotMove();
		car.setRotation(newRot);
	}

	private void updateVerticalPosition(boolean accelerating, Car car)
	{
		double rotation = car.getRotation();
		double rotationRadians = Math.toRadians(rotation);
		
		double xMove = car.getXMove();
		double yMove = car.getYMove();
		if (accelerating)
		{
			yMove = MOVEMENT_AMOUNT*Math.cos(rotationRadians);
			xMove = MOVEMENT_AMOUNT*Math.sin(rotationRadians);
		}
		else
		{
			yMove = Math.abs(yMove)*Math.cos(rotationRadians);
			xMove = Math.abs(xMove)*Math.sin(rotationRadians);
		}
		car.setYMove(yMove);
		car.setXMove(xMove);
		
		SpriteHandler sprites = car.getSprites();
		sprites.resolveCollisions();
		
		double oldY = car.getPosY();
		double newY = oldY - yMove;
		double oldX = car.getPosX();
		double newX = oldX + xMove;
		
		if (!car.getCollisionHappened())
		{
			if (newY > 0)
			{
				car.setPosY(newY);
				car.updateProgressBar();
			}
			if (newX > 0 && newX < Car.GAME_CANVAS_WIDTH - Car.HEIGHT)
			{
				car.setPosX(newX);
			}
		}
		
		if (newY < TOP_BUFFER)
//		  || !singlePlayer && car.getTrackDisposition() < trackDispLargest && newY < TOP_BUFFER + trackDispLargest - car.getTrackDisposition())
		{
			car.setCarCloseToTop(true);
		}
	}

	public static int getRoadNumberCoefficient()
	{
		return roadNumberCoefficient;
	}
	public static boolean getGameWon()
	{
		return gameWon;
	}
	public static void setGameWon()
	{
		gameWon = true;
	}	
	public static double getTrackDistance()
	{
		return trackDistance;
	}
}
