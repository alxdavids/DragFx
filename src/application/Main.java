package application;
	
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import sprite.Boost;
import sprite.Car;
import sprite.Car.Speed;
import sprite.FinishLine;
import sprite.PowerUp;
import sprite.Road;
import sprite.SlowDown;
import sprite.Sprite;
import sprite.SpriteHandler;
import sprite.TimeSlow;
import sprite.Wall;
import utils.RestrictiveTextField;


public class Main extends Application 
{
	//If these change you should probably think about resetting the leaderboard.
	public enum GameLength {
		LONG(-15), SHORT(-8);
		
		private int value;
		
		private GameLength(int value)
		{
			this.value = value;
		}
	}	
	public enum CarColorHtml {
		CAR_YELLOW_HTML("#FFC601"), CAR_BLUE_HTML("#2490FB"), CAR_GREEN_HTML("#51d173"), CAR_RED_HTML("#bf2c40"), 
		CAR_WHITE_HTML("#ffffff");
		
		private String colorHexCode;
		
		private CarColorHtml(String colorHexCode)
		{
			this.colorHexCode = colorHexCode;
		}
		public String getColorHexCode()
		{
			return colorHexCode;
		}
	}	
	public enum CarColor {
		CAR_YELLOW(CarColorHtml.CAR_YELLOW_HTML.colorHexCode), CAR_BLUE(CarColorHtml.CAR_BLUE_HTML.colorHexCode), 
		CAR_GREEN(CarColorHtml.CAR_GREEN_HTML.colorHexCode), CAR_RED(CarColorHtml.CAR_RED_HTML.colorHexCode),
		CAR_WHITE(CarColorHtml.CAR_WHITE_HTML.colorHexCode);
		
		private Color color;
		
		private CarColor(String colorHexCode)
		{
			this.color = Color.web(colorHexCode);
		}	
		public Color getColor()
		{
			return color;
		}
	}
	public enum FilePaths {
		DRAGFX_FOLDER("D:\\DragFx"), DRAGFX_LEADERBOARD("D:\\DragFx\\Leaderboard.txt");
		
		private String filePath;
		
		private FilePaths(String filePath)
		{
			this.filePath = filePath;
		}
		public String getFilePath()
		{
			return filePath;
		}
	}
	public enum Styles {
		BOLD("-fx-font-weight: bold");
		
		private String style;
		
		private Styles(String style)
		{
			this.style = style;
		}
		public String getStyle()
		{
			return style;
		}
	}
	public enum TimeGap {
		SLOW(0.01),NORMAL(0.1);
		
		private double time;
		
		private TimeGap(double time)
		{
			this.time = time;
		}
		public double getTime()
		{
			return time;
		}
	}
	
	public static final int TOP_BUFFER = 200;
	
	private AnimationTimer animTimer;
	private Label timerText = null;
	private boolean longGame = false;
	private static boolean gameWon = false;
	private boolean testMode = false;
	private double time = 0;
	private Timeline timer = null;
	private double raceTime = 0;
	private GridPane topBar = null;
	private Vector<Car> cars = null;
	private boolean started = false;
	private String winnerName = "";
	private double timeGap = TimeGap.NORMAL.getTime();
	
	private static boolean singlePlayer = true;
	
	private Player playerOne = null;
	private Player playerTwo = null;
	
	private static int roadNumberCoefficient = 1; //1 builds zero roads. Decrease to build more roads.
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
		HBox hbBtnMulti = new HBox(10);
		hbBtnMulti.getChildren().add(btnMultiPlayer);
		
		Button btnLeaderboard = new Button("Leaderboard");
		HBox hbBtnLeaderboard = new HBox(10);
		hbBtnLeaderboard.getChildren().add(btnLeaderboard);
		
		setInitialButtonActions(btnSinglePlayer, btnMultiPlayer, primaryStage);
		setLeaderboardButtonActions(btnLeaderboard, primaryStage);
		
		btnPane.setTop(btnSinglePlayer);
		btnPane.setCenter(btnMultiPlayer);
		btnPane.setBottom(btnLeaderboard);
		setCentralAlignment(btnSinglePlayer, btnLeaderboard);
		
		Scene initialScene = new Scene(btnPane, 300, 200);
		return initialScene;
	}

	private void setCentralAlignment(Button btnSinglePlayer, Button btnLeaderboard)
	{
		BorderPane.setAlignment(btnSinglePlayer, Pos.CENTER);
		BorderPane.setAlignment(btnLeaderboard, Pos.CENTER);
	}

	private void setInitialButtonActions(Button btnSinglePlayer, Button btnMultiPlayer, Stage primaryStage)
	{		
		btnSinglePlayer.setOnAction( (e) -> {
			singlePlayer = true;
			selectOptions(primaryStage);
		}) ;
				
		btnMultiPlayer.setOnAction( (e) -> {
			singlePlayer = false;
			selectOptions(primaryStage);
		});		
	}
	
	private void setLeaderboardButtonActions(Button btnLeaderboard, Stage primaryStage)
	{
		btnLeaderboard.setOnAction( (e) -> {
			viewLeaderboard(primaryStage, longGame);
		});
	}

	private void viewLeaderboard(Stage primaryStage, boolean viewLongTimes)
	{
		GridPane leaderboard = new GridPane();
		leaderboard.setAlignment(Pos.CENTER);
		leaderboard.setHgap(10);
		leaderboard.setVgap(10);
		leaderboard.setPadding(new Insets(25, 25, 25, 25));
		leaderboard.setPrefSize(Car.CanvasDimension.GAME_CANVAS_WIDTH.getValue(), 500);
		
		initialiseLeaderboard(primaryStage, viewLongTimes, leaderboard);
		
		Scene leaderboardScene = new Scene(leaderboard, 300, 500);
		leaderboardScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		primaryStage.setScene(leaderboardScene);
		primaryStage.show();
	}

	private void initialiseLeaderboard(Stage primaryStage,
			boolean viewLongTimes, GridPane leaderboard)
	{
		Text scenetitle = new Text("DragFx - Leaderboard");
		scenetitle.setId("dragfx-text");
		leaderboard.add(scenetitle, 0, 0, 2, 1);
		
		Text nameColumn = new Text("Name");
		nameColumn.setStyle(Styles.BOLD.getStyle());
		leaderboard.add(nameColumn, 0, 1);
		
		Text timeColumn = new Text("Time");
		timeColumn.setStyle(Styles.BOLD.getStyle());
		leaderboard.add(timeColumn, 2, 1);
		
		ArrayList<Vector<String>> leaderboardEntries = readLeaderboardFile(viewLongTimes);
		int maxSize = 10;
		if (leaderboardEntries == null)
		{
			maxSize = 0;
		}
		else if (leaderboardEntries.size() < maxSize)
		{
			maxSize = leaderboardEntries.size();
		}
		
		for (int i=0; i<maxSize; i++)
		{
			Vector<String> entry = leaderboardEntries.get(i);
			
			String name = entry.elementAt(0);
			String time = entry.elementAt(1);
			Text leaderboardName = new Text(name);
			Text leaderboardTime = new Text(time);
			
			leaderboard.add(leaderboardName, 0, i+2, 2, 1);
			leaderboard.add(leaderboardTime, 2, i+2);
		}
		
		Label view = new Label("View");
		view.setStyle(Styles.BOLD.getStyle());
		
		Button btnShortTimes = new Button("Short");
		btnShortTimes.setDefaultButton(true);
		HBox hbBtnShortTimes = new HBox(10);
		hbBtnShortTimes.getChildren().add(btnShortTimes);
		
		Button btnLongTimes = new Button("Long");
		HBox hbBtnLongTimes = new HBox(10);
		hbBtnLongTimes.getChildren().add(btnLongTimes);
		
		Button btnMainMenu = new Button("Main menu");
		HBox hbBtnMainMenu = new HBox(10);
		hbBtnMainMenu.getChildren().add(btnMainMenu);
		
		setViewButtonActions(btnShortTimes, btnLongTimes, primaryStage);
		setMainMenuButtonAction(btnMainMenu, primaryStage);
		
		leaderboard.add(view, 0, maxSize+3);
		leaderboard.add(btnShortTimes, 1, maxSize+3);
		leaderboard.add(btnLongTimes, 2, maxSize+3);
		leaderboard.add(btnMainMenu, 2, maxSize+4);
	}

	private void setMainMenuButtonAction(Button btnMainMenu, Stage primaryStage)
	{
		btnMainMenu.setOnAction( (e) -> {
			resetVariables();
			start(primaryStage);
		});
	}

	private void resetVariables()
	{
		gameWon = false;
		time = 0;
		started = false;
		winnerName = "";
		longGame = false;
		timeGap = TimeGap.NORMAL.getTime();
		raceTime = 0;
	}

	private void setViewButtonActions(Button btnShortTimes, Button btnLongTimes, Stage primaryStage)
	{
		btnShortTimes.setOnAction( (e) -> {
			viewLeaderboard(primaryStage, false);
		});
		
		btnLongTimes.setOnAction( (e) -> {
			viewLeaderboard(primaryStage, true);
		});
	}

	private ArrayList<Vector<String>> readLeaderboardFile(boolean viewLongTimes)
	{
		File file = new File(FilePaths.DRAGFX_LEADERBOARD.getFilePath());
		
		if (!file.exists())
		{
		    return null;
		}

		ArrayList<Vector<String>> leaderboardEntries = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file)))
		{		    
			String line = br.readLine();
			while (line != null)
			{
				int indexOfName = line.indexOf(":");
				int indexOfTime = line.indexOf(":", indexOfName+1);
				int indexOfTimeString = line.indexOf("Time:");
				int indexOfType = line.lastIndexOf(":");
				int indexOfTypeString = line.indexOf("Type:");

				if (indexOfName > -1)
				{
					String nameString = line.substring(indexOfName+1, indexOfTimeString-1);
					String timeString = line.substring(indexOfTime+1, indexOfTypeString-1);
					String typeString = line.substring(indexOfType+1);
					String timeTrimmed = timeString.trim();
					typeString = typeString.trim();
					
					if (viewLongTimes && typeString.equals("long"))
					{
						addToLeaderboardEntries(leaderboardEntries, nameString, timeTrimmed);
					}
					else if (!viewLongTimes && typeString.equals("short"))
					{
						addToLeaderboardEntries(leaderboardEntries, nameString, timeTrimmed);
					}						 
				}		
				line = br.readLine();
		    }
		} 
		catch (IOException e) 
		{
		    e.printStackTrace();
		}		
		
		return leaderboardEntries;
	}

	private void addToLeaderboardEntries(ArrayList<Vector<String>> leaderboardEntries, String nameString,
										String timeTrimmed)
	{
		Vector<String> nameAndTime = new Vector<>();
		nameAndTime.add(nameString);
		nameAndTime.add(timeTrimmed);
		leaderboardEntries.add(nameAndTime);
	}

	private void selectOptions(Stage primaryStage)
	{
		GridPane entryGrid = new GridPane();
		entryGrid.setAlignment(Pos.CENTER);
		entryGrid.setHgap(10);
		entryGrid.setVgap(10);
		entryGrid.setPadding(new Insets(25, 25, 25, 25));
		entryGrid.setPrefSize(335, 300);
		
		initialiseEntryPane(entryGrid, primaryStage);
		
		Scene entryScene = new Scene(entryGrid, 335, 300);
		entryScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		primaryStage.setScene(entryScene);
		primaryStage.show();
	}

	private void startGame(Stage primaryStage)
	{
		Vector<Player> players = new Vector<>();
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
		topBar.setVgap(2);
		topBar.setHgap(10);
		if (singlePlayer)
		{
			topBar.setPrefSize(Car.CanvasDimension.GAME_CANVAS_WIDTH.getValue(), 50);
		}
		else
		{
			topBar.setPrefSize(Car.CanvasDimension.GAME_CANVAS_WIDTH.getValue(), 50);
		}
		
		timerText = new Label();
		timerText.setId("timer-text");
		topBar.add(timerText, 0, 0);
						
		HBox canvasHBox = new HBox();
		Vector<Group> progressBars = new Vector<>();
		
		cars.forEach( (car) -> {
			Group progressBar = car.initProgressBar();
			progressBars.add(progressBar);
			initProgressBar(car);
			canvasHBox.getChildren().add(car.getGameCanvas());
			drawGame(car);
		});
		
		HBox hBox = new HBox();
		for (Group progressBar : progressBars)
		{
			hBox.getChildren().add(progressBar);
		}
		
		Scene gameScene = new Scene(new VBox(topBar, new HBox(canvasHBox, hBox)));	
		gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				
		gameScene.setOnKeyPressed( (e) ->  {
			handleKeyPressed(e);
		});
		
		gameScene.setOnKeyReleased( (e) -> {
				handleKeyReleased(e);
		});
		
		primaryStage.setScene(gameScene);
		primaryStage.show();	
		
		animTimer = new AnimationTimer() 
		{
			public void handle(long arg0) 
			{
				update(gameScene, primaryStage);
			}			
		};
		
		timer = new Timeline(
				new KeyFrame(Duration.seconds(0), (e) -> {
					if (time < 1.5)
					{
						timerText.setText("Get ready!");
					}
					else if (time > 1.5 && time < 2)
					{
						timerText.setText("Go!");
					}
					else
					{
						if (!started)
						{
							animTimer.start();
							started = true;
						}
						double timeToDisplay = time-2;
						timerText.setText("" + timeToDisplay);
						raceTime = raceTime + 0.1;
					}
					time = time + timeGap;
				}),
				new KeyFrame(Duration.seconds(TimeGap.NORMAL.getTime()))
		);
		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();
	}

	private void initProgressBar(Car car)
	{
		double carY = car.getPosY();
		trackDistance = carY-car.getSprites().getTrackEnd();
		car.updateProgressBar();
	}

	private void update(Scene gameScene, Stage primaryStage)
	{
		if (!gameWon)
		{			
			cars.forEach( (car) -> {
				setPowerUpForCar(car);								
				updatePosition(car);
				if (car.getCarCloseToTop() && !car.getReachedEndOfTrack())
				{
					scrollScreen(car, car.getSprites());
				}
			});
		}
		else 
		{
			gameScene.setOnKeyPressed(null);
			gameScene.setOnKeyReleased(null);
			
			animTimer.stop();
			timer.stop();		
			
			writeTimeToLeaderboard();
			
			addButtonsToTopBar(primaryStage);
		}
	}

	private void setPowerUpForCar(Car car)
	{
		ConcurrentHashMap<PowerUp,Double> powerUps = car.getPowerUps();
		if (!powerUps.isEmpty())
		{
			//Concurrent modification exception
			powerUps.forEach( (powerUp, time) -> {
				// +5 allows power ups to be in action for 5 seconds (since time is 2 seconds behind 
				// the time game has been running for.
				if (time+3 > raceTime)
				{
					if (powerUp instanceof Boost)
					{
						car.setCurrentSpeed(Car.Speed.BOOST_MOVEMENT_SPEED.getValue());
					}		
					else if (powerUp instanceof SlowDown)
					{
						car.setCurrentSpeed(Car.Speed.SLOW_MOVEMENT_SPEED.getValue());
					}
					else if (powerUp instanceof TimeSlow)
					{
						setTimeGap(TimeGap.SLOW.getTime());
					}
				}
				else
				{
					if (powerUp instanceof Boost || powerUp instanceof SlowDown)
					{
						car.setCurrentSpeed(Car.Speed.NORMAL_MOVEMENT_SPEED.getValue());
						powerUps.remove(powerUp, time);
					}
					else if (powerUp instanceof TimeSlow)
					{
						setTimeGap(TimeGap.NORMAL.getTime());
						powerUps.remove(powerUp, time);
					}
				}
			});
		}
		else
		{
			car.setCurrentSpeed(Speed.NORMAL_MOVEMENT_SPEED.getValue());
		}
	}	
	
	private void addButtonsToTopBar(Stage primaryStage)
	{
		Button btnMainMenu = new Button("Main menu");
		HBox hbBtnMainMenu = new HBox(10);
		hbBtnMainMenu.getChildren().add(btnMainMenu);
		hbBtnMainMenu.setAlignment(Pos.CENTER_RIGHT);
		
		Button btnLeaderboard = new Button("Leaderboard");
		btnLeaderboard.setDefaultButton(true);
		HBox hbBtnLeaderboard = new HBox(10);
		hbBtnLeaderboard.getChildren().add(btnLeaderboard);
		hbBtnLeaderboard.setAlignment(Pos.CENTER_RIGHT);
		
		setMainMenuButtonAction(btnMainMenu, primaryStage);
		setLeaderboardButtonActions(btnLeaderboard, primaryStage);
		
		// spacers
		final Pane pane1 = new Pane();
		final Pane pane2 = new Pane();
		pane1.setPrefWidth(30);
		pane2.setPrefWidth(30);
		
		topBar.add(pane1, 2, 0);
		topBar.add(pane2, 2, 1);
		
		topBar.add(hbBtnLeaderboard, 3, 0);
		topBar.add(hbBtnMainMenu, 3, 1);	
	}

	private void writeTimeToLeaderboard()
	{
		String timeString = timerText.getText();
		double time = Double.parseDouble(timeString);	
			
		try
		{
			File file = getLeaderboardFile();	
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			ArrayList<String> lines = new ArrayList<>();
			
			boolean nullLines = true;
			boolean lineAdded = false;
			while (line != null)
			{
				nullLines = false;
				int indexOfTimeStart = line.indexOf("Time:");
				int indexOfScore = line.indexOf(":", indexOfTimeStart+1);
				int indexOfTypeStart = line.indexOf("Type:");
				if (indexOfScore > -1)
				{
				    String scoreString = line.substring(indexOfScore+1, indexOfTypeStart-1);
				    String scoreTrimmed = scoreString.trim();
				    double timeToCheck = Double.parseDouble(scoreTrimmed);
				    if (time < timeToCheck
				      && !lineAdded)
				    {
				    	addNewTime(time, lines);
				    	lineAdded = true;
				    	lines.add(line);
				    }
				    else
				    {
				    	lines.add(line);
				    }
				}
				line = br.readLine();
			}
			br.close();
			
			if (nullLines || !lineAdded)
			{
				addNewTime(time, lines);
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (String lineToWrite : lines)
			{
				bw.write(lineToWrite);
			    bw.newLine();
			}
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void addNewTime(double time, ArrayList<String> lines)
	{
		if (!testMode)
		{
			if (longGame)
			{
				lines.add("Name:" + winnerName + " Time:" + time + " Type:long");
			}
			else
			{
				lines.add("Name:" + winnerName + " Time:" + time + " Type:short");
			}
		}
	}

	private File getLeaderboardFile() throws IOException
	{
		File folder = new File(FilePaths.DRAGFX_FOLDER.getFilePath());
		File file = new File(FilePaths.DRAGFX_LEADERBOARD.getFilePath());

		if (!folder.exists())   
		{
			folder.mkdir();		
			file.createNewFile();
		}
		else if (!file.exists())
		{
			file.createNewFile();
		}
		
		return file;
	}

	private void setRoadNumberCoefficient()
	{
		if (testMode)
		{
			roadNumberCoefficient = -3;
		}
		else if (longGame)
		{
			roadNumberCoefficient = GameLength.LONG.value;
		}
		else
		{
			roadNumberCoefficient = GameLength.SHORT.value;
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
		
		ComboBox<Color> cmbP1 = addCarColorComboBox();
		Label lblP1 = new Label("Choose car colour for player 1: ");
		entryGrid.add(new HBox(lblP1, cmbP1), 0, 2, 2, 1);
		
		Label nameTwo = new Label("Player two name:");
		entryGrid.add(nameTwo, 0, 3);
		
		RestrictiveTextField nameTwoTextField = new RestrictiveTextField();
		nameTwoTextField.setMaxLength(12);
		entryGrid.add(nameTwoTextField, 1, 3);
		
		ComboBox<Color> cmbP2 = addCarColorComboBox();
		Label lblP2 = new Label("Choose car colour for player 2: ");
		entryGrid.add(new HBox(lblP2, cmbP2), 0, 4, 2, 1);
		
		Button btnLong = new Button("Long game");
		HBox hbBtnLong = new HBox(10);
		hbBtnLong.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtnLong.getChildren().add(btnLong);
		entryGrid.add(hbBtnLong, 1, 5);
		
		Button btnShort = new Button("Short game");
		btnShort.setDefaultButton(true);
		HBox hbBtnShort = new HBox(10);
		hbBtnShort.setAlignment(Pos.BOTTOM_LEFT);
		hbBtnShort.getChildren().add(btnShort);
		entryGrid.add(hbBtnShort, 0, 5);
		
		final Text playerInstruction = new Text();
		playerInstruction.setText("Player one uses WAD controls.");
		entryGrid.add(playerInstruction, 0, 6, 2, 1);		
		
		Button btnMainMenu = new Button("Main menu");
		HBox hbBtnMainMenu = new HBox(10);
		hbBtnMainMenu.getChildren().add(btnMainMenu);
		hbBtnMainMenu.setAlignment(Pos.BOTTOM_RIGHT);
		entryGrid.add(hbBtnMainMenu, 1, 7);
		
		setMainMenuButtonAction(btnMainMenu, primaryStage);	
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 0, 8, 2, 1);
		
        setMultiPlayerButtonActions(primaryStage, nameOneTextField, nameTwoTextField, btnLong, btnShort, errorAction, cmbP1, cmbP2);
	}

	private void setMultiPlayerButtonActions(Stage primaryStage, RestrictiveTextField nameOneTextField, RestrictiveTextField nameTwoTextField, Button btnLong, 
								Button btnShort, Text errorAction, ComboBox<Color> cmbP1, ComboBox<Color> cmbP2)
	{
		btnLong.setOnAction( (e) -> {
			longGame = true;
			initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction, cmbP1, cmbP2);
		});
		
		btnShort.setOnAction( (e) -> {
			initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction, cmbP1, cmbP2);
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
		
		ComboBox<Color> cmb = addCarColorComboBox();        
        Label lbl = new Label("Choose car colour: ");        
        entryGrid.add(new HBox(lbl, cmb), 0, 5, 2, 1);
		
		Button btnLong = new Button("Long game");
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
		
		Button btnMainMenu = new Button("Main menu");
		HBox hbBtnMainMenu = new HBox(10);
		hbBtnMainMenu.getChildren().add(btnMainMenu);
		hbBtnMainMenu.setAlignment(Pos.BOTTOM_RIGHT);
		entryGrid.add(hbBtnMainMenu, 1, 7);
		
		setMainMenuButtonAction(btnMainMenu, primaryStage);		
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 0, 8, 2, 1);
		
		setSinglePlayerButtonActions(primaryStage, nameTextField, btnLong, btnShort, cBox, errorAction, cmb);
	}

	private ComboBox<Color> addCarColorComboBox()
	{		
		ComboBox<Color> cmb = new ComboBox<Color>();
        cmb.getItems().addAll(CarColor.CAR_YELLOW.color, CarColor.CAR_BLUE.color, 
        					CarColor.CAR_GREEN.color, CarColor.CAR_RED.color,
        					CarColor.CAR_WHITE.color);
        
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
        
        //render selected colour in combo box
        cmb.setButtonCell(cmb.getCellFactory().call(null));
        
        return cmb;
	}

	private void setSinglePlayerButtonActions(Stage primaryStage, RestrictiveTextField nameTextField, Button btnLong, Button btnShort, CheckBox cBox, 
			final Text errorAction, ComboBox<Color> cmb)
	{
		btnLong.setOnAction( (e) -> {
			longGame = true;
			initialiseSinglePlayerGame(primaryStage, nameTextField, cBox, errorAction, cmb);
		});
		
		btnShort.setOnAction( (e) -> {
			initialiseSinglePlayerGame(primaryStage, nameTextField, cBox, errorAction, cmb);
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

	private void initialiseMultiPlayerGame(Stage primaryStage, RestrictiveTextField nameOneTextField, RestrictiveTextField nameTwoTextField, Text errorAction, 
										ComboBox<Color> cmbP1, ComboBox<Color> cmbP2)
	{
		errorAction.setText("");
		errorAction.setFill(Color.FIREBRICK);
		
		playerOne = new Player();
		playerTwo = new Player();
		
		String nameOne = nameOneTextField.getText();
		String nameTwo = nameTwoTextField.getText();
		
		if (nameOne.isEmpty() || nameTwo.isEmpty())
		{
			errorAction.setText("A player has not entered a name");
			return;
		}
		else if (nameOne.equals(nameTwo))
		{
			errorAction.setText("Players must have different names");
			return;
		}
		
		Color colorP1 = cmbP1.getValue();
		Color colorP2 = cmbP2.getValue();
		if (colorP1 == null || colorP2 == null)
		{
			errorAction.setText("Both players haven't selected a colour for their cars");
			return;
		}
		else if (colorP1.equals(colorP2))
		{
			errorAction.setText("Players must select different colours");
			return;
		}
		
		playerOne.setUseAlternateControls(true);
		playerOne.setCarColor(colorP1);
		playerTwo.setCarColor(colorP2);
		
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
		cars.forEach( (car) -> {
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
		});
	}
	
	private void handleKeyPressed(KeyEvent event)
	{
		cars.forEach( (car) -> {
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
		});
	}

	private void createSprites()
	{
		SpriteHandler sprites = new SpriteHandler(roadNumberCoefficient);
		SpriteHandler spritesCopy = null;
		cars = new Vector<>();

		Color carColor = playerOne.getCarColor();
		Image carImage = setCarColor(carColor);
		Car car = new Car(carImage, 147.5, 650, 0, playerOne);			
		sprites.add(car);
		cars.add(car);

		Image roadImage = new Image(this.getClass().getResource("Road.png").toString());
		Image wallImage = new Image(this.getClass().getResource("Wall.png").toString());
		Image finishLineImage = new Image(this.getClass().getResource("FinishLine.png").toString());
				
		//Remember that anything that you want to add here also has to be added to the method that draws the components
		addRoads(roadImage, sprites);		
		addWalls(wallImage, sprites);				
		addFinishLine(finishLineImage, sprites);
		addPowerUps(sprites);
		checkSpritesArePlacedCorrectly(sprites);

		car.setSprites(sprites);
		
		if (!singlePlayer)
		{
			spritesCopy = createSpritesVectorCopy(sprites);
			Car carTwo = (Car) spritesCopy.elementAt(0);
			cars.add(carTwo);
			carTwo.setSprites(spritesCopy);
		}				
	}

	private Image setCarColor(Color carColor)
	{
		Image carImage = null;
		if (carColor.equals(CarColor.CAR_YELLOW.color))
		{
			carImage = new Image(this.getClass().getResource("CarYellow.png").toString());
		}
		else if (carColor.equals(CarColor.CAR_BLUE.color))
		{
			carImage = new Image(this.getClass().getResource("CarBlue.png").toString());
		}			
		else if (carColor.equals(CarColor.CAR_GREEN.color))
		{
			carImage = new Image(this.getClass().getResource("CarGreen.png").toString());
		}
		else if (carColor.equals(CarColor.CAR_RED.color))
		{
			carImage = new Image(this.getClass().getResource("CarRed.png").toString());
		}
		else if (carColor.equals(CarColor.CAR_WHITE.color))
		{
			carImage = new Image(this.getClass().getResource("CarWhite.png").toString());
		}
		return carImage;
	}

	private void addPowerUps(SpriteHandler sprites)
	{			
		addBoosts(sprites);		
		addSlowDowns(sprites);
		
		if (singlePlayer)
		{
			addTimeSlows(sprites);
		}
	}

	private void addTimeSlows(SpriteHandler sprites)
	{
		Image tsImage = new Image(this.getClass().getResource("TimeSlow.png").toString());
		double numberOfTimeSlows = (Math.abs(roadNumberCoefficient)/5);
		if (testMode
		  && numberOfTimeSlows == 0)
		{
			numberOfTimeSlows = 1;
		}
		
		for (int i=0; i<numberOfTimeSlows; i++)
		{
			double rndY = TimeSlow.getRandomYCoordinate(roadNumberCoefficient);
			double rndX = TimeSlow.getRandomXCoordinate();
			
			TimeSlow ts = new TimeSlow(tsImage, rndX, rndY);
			sprites.add(ts);
		}
	}

	private void addSlowDowns(SpriteHandler sprites)
	{
		Image slowDownImage = new Image(this.getClass().getResource("SpeedSlowDown.png").toString());
		double numberOfSlowdowns = (Math.abs(roadNumberCoefficient)/4);
		if (numberOfSlowdowns == 0)
		{
			numberOfSlowdowns = 1;
		}

		for (int i=0; i<numberOfSlowdowns; i++)
		{
			double rndY = SlowDown.getRandomYCoordinate(roadNumberCoefficient);
			double rndX = SlowDown.getRandomXCoordinate();

			SlowDown slowDown = new SlowDown(slowDownImage, rndX, rndY);
			sprites.add(slowDown);
		}
	}

	private void addBoosts(SpriteHandler sprites)
	{
		Image boostImage = new Image(this.getClass().getResource("SpeedBoost.png").toString());
		double numberOfBoosts = (Math.abs(roadNumberCoefficient)/3);
		if (numberOfBoosts == 0)
		{
			numberOfBoosts = 1;
		}		
		for (int i=0; i<numberOfBoosts; i++)
		{
			double rndY = Boost.getRandomYCoordinate(roadNumberCoefficient);
			double rndX = Boost.getRandomXCoordinate();
						
			Boost boost = new Boost(boostImage, rndX, rndY);
			sprites.add(boost);
		}
	}

	private SpriteHandler createSpritesVectorCopy(SpriteHandler sprites)
	{
		SpriteHandler spritesCopy = new SpriteHandler(roadNumberCoefficient);
		
		sprites.forEach( (sprite) -> {
			if (sprite instanceof Car)
			{				
				Color carColor = playerTwo.getCarColor();
				Image carImage = setCarColor(carColor);
				Car car = new Car(carImage, sprite.getPosX(), sprite.getPosY(), ((Car) sprite).getRotation(), playerTwo);
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
			else if (sprite instanceof Boost)
			{
				Boost powerUp = new Boost(sprite.getImage(), sprite.getPosX(), sprite.getPosY());
				spritesCopy.add(powerUp);
			}
			else if (sprite instanceof SlowDown)
			{
				SlowDown powerUp = new SlowDown(sprite.getImage(), sprite.getPosX(), sprite.getPosY());
				spritesCopy.add(powerUp);
			}
			else if (sprite instanceof TimeSlow)
			{
				TimeSlow powerUp = new TimeSlow(sprite.getImage(), sprite.getPosX(), sprite.getPosY());
				spritesCopy.add(powerUp);
			}
		});
		
		return spritesCopy;
	}

	private void addFinishLine(Image finishLineImage, SpriteHandler sprites)
	{
		FinishLine finishLine = new FinishLine(0, (roadNumberCoefficient+1)*Road.Dimension.HEIGHT.getValue() + Car.Dimension.HEIGHT.getValue(), finishLineImage);
		sprites.add(finishLine);
	}

	private void checkSpritesArePlacedCorrectly(SpriteHandler sprites)
	{
		boolean spritesInValidPositions = false;
		while (!spritesInValidPositions)
		{
			spritesInValidPositions = sprites.checkSpritesArePlacedCorrectly();
		}
	}

	private void addRoads(Image roadImage, SpriteHandler sprites)
	{
		for (int i=1; i>roadNumberCoefficient; i--)
		{
			Road road = new Road(0, i*Road.Dimension.HEIGHT.getValue(), roadImage);
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
		
		for (Sprite sprite : sprites)
		{
			if (sprite instanceof Road)
			{
				Road road = (Road) sprite;
				gc.drawImage(road.getImage(), road.getPosX(), road.getPosY());				
				if (road.getPosY() < 0)
				{
					reachedEndOfTrack = false;
				}
			}
		}

		sprites.forEach( (sprite) -> {
			if (!(sprite instanceof Road)
			  && !(sprite instanceof Car))
			{
				gc.drawImage(sprite.getImage(), sprite.getPosX(), sprite.getPosY());
			}
		});
		
		if (reachedEndOfTrack)
		{
			car.setReachedEndOfTrack(true);
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
			for (Car checkCar : cars)
			{
				if (checkCar.getWinner())
				{
					Label winnerText = new Label();
					winnerText.setId("winner-text");
					winnerName = checkCar.getPlayer().getName();
					
					if (singlePlayer)
					{
						winnerText.setText("Good time " + winnerName + "!");
					}
					else
					{
						winnerText.setText(winnerName + " is the winner!");
					}
						
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
		gc.translate(x + (Car.Dimension.WIDTH.getValue())/2, y + (Car.Dimension.HEIGHT.getValue())/2);
		gc.rotate(car.getRotation());

		gc.drawImage(car.getImage(), -(Car.Dimension.WIDTH.getValue())/2, -(Car.Dimension.HEIGHT.getValue())/2);

		gc.restore();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	private void updatePosition(Car car)
	{
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
			double currentSpeed = car.getCurrentSpeed();
			yMove = currentSpeed*Math.cos(rotationRadians);
			xMove = currentSpeed*Math.sin(rotationRadians);
		}
		else
		{
			yMove = Math.abs(yMove)*Math.cos(rotationRadians);
			xMove = Math.abs(xMove)*Math.sin(rotationRadians);
		}
		car.setYMove(yMove);
		car.setXMove(xMove);
		
		SpriteHandler sprites = car.getSprites();
		// Pass in time-2 as this is the actual time that is displayed.
		sprites.resolveCollisions(raceTime);
		
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
			if (newX > 0 && newX < Car.CanvasDimension.GAME_CANVAS_WIDTH.getValue() - Car.Dimension.HEIGHT.getValue())
			{
				car.setPosX(newX);
			}
		}
		
		if (newY < TOP_BUFFER)
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
	
	public double getTimeGap()
	{
		return timeGap;
	}
	public void setTimeGap(double timeGap)
	{
		this.timeGap = timeGap;
	}
}
