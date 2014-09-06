package application;
	
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

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
import sprite.Car;
import sprite.FinishLine;
import sprite.Road;
import sprite.Sprite;
import sprite.SpriteHandler;
import sprite.Wall;
import utils.RestrictiveTextField;


public class Main extends Application 
{
	public enum GameLength {
		LONG(-15), SHORT(-8);
		
		private int value;
		
		private GameLength(int value)
		{
			this.value = value;
		}
	}	
	public enum CarColorHtml {
		CAR_YELLOW_HTML("#FFC601"), CAR_BLUE_HTML("#2490FB");
		
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
		CAR_YELLOW(CarColorHtml.CAR_YELLOW_HTML.colorHexCode), CAR_BLUE(CarColorHtml.CAR_BLUE_HTML.colorHexCode);
		
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
	
	private static final double MOVEMENT_AMOUNT = 4.5;
	public static final int TOP_BUFFER = 200;
	private static final double TIME_GAP = 0.1;
	
	private AnimationTimer animTimer;
	private Label timerText = null;
	private boolean endOfTrack = false;
	private boolean longGame = false;
	private static boolean gameWon = false;
	private boolean testMode = false;
	private double time = 0;
	private Timeline timer = null;
	private GridPane topBar = null;
	private Vector<Car> cars = null;
	private boolean started = false;
	private String winnerName = "";
	
	private static boolean singlePlayer = true;
	
	private Player playerOne = null;
	private Player playerTwo = null;
	
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
		
		Scene leaderboardScene = new Scene(leaderboard, 300, 500);
		leaderboardScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		primaryStage.setScene(leaderboardScene);
		primaryStage.show();
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
		endOfTrack = false;
		time = 0;
		started = false;
		winnerName = "";
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

	private void addToLeaderboardEntries(
			ArrayList<Vector<String>> leaderboardEntries, String nameString,
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
		
		initialiseEntryPane(entryGrid, primaryStage);
		
		Scene entryScene = new Scene(entryGrid, 300, 275);
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
		topBar.setPrefSize(Car.GAME_CANVAS_WIDTH, 50);
		
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
					}
					time = time + TIME_GAP;
				}),
				new KeyFrame(Duration.seconds(TIME_GAP))
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
			updatePosition();
			cars.forEach( (car) -> {
				if (car.getCarCloseToTop() && !endOfTrack)
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
			roadNumberCoefficient = -2;
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
		
		Label nameTwo = new Label("Player two name:");
		entryGrid.add(nameTwo, 0, 2);
		
		RestrictiveTextField nameTwoTextField = new RestrictiveTextField();
		nameTwoTextField.setMaxLength(12);
		entryGrid.add(nameTwoTextField, 1, 2);
		
		Button btnLong = new Button("Long game");
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
		
		final Text playerInstruction = new Text();
		playerInstruction.setText("Player one uses WAD controls.");
		entryGrid.add(playerInstruction, 0, 4, 2, 1);		
		
		Button btnMainMenu = new Button("Main menu");
		HBox hbBtnMainMenu = new HBox(10);
		hbBtnMainMenu.getChildren().add(btnMainMenu);
		hbBtnMainMenu.setAlignment(Pos.BOTTOM_RIGHT);
		entryGrid.add(hbBtnMainMenu, 1, 5);
		
		setMainMenuButtonAction(btnMainMenu, primaryStage);	
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 0, 6, 2, 1);
		
        setMultiPlayerButtonActions(primaryStage, nameOneTextField, nameTwoTextField, btnLong, btnShort, errorAction);
	}

	private void setMultiPlayerButtonActions(Stage primaryStage, RestrictiveTextField nameOneTextField, RestrictiveTextField nameTwoTextField, Button btnLong, 
								Button btnShort, Text errorAction)
	{
		btnLong.setOnAction( (e) -> {
			longGame = true;
			initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction);
		});
		
		btnShort.setOnAction( (e) -> {
			initialiseMultiPlayerGame(primaryStage, nameOneTextField, nameTwoTextField, errorAction);
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

	private ComboBox<Color> addCarColorComboBox(GridPane entryGrid)
	{		
		ComboBox<Color> cmb = new ComboBox<Color>();
        cmb.getItems().addAll(CarColor.CAR_YELLOW.color, CarColor.CAR_BLUE.color);
        
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
        
        Label lbl = new Label("Choose car colour: ");
        
        entryGrid.add(new HBox(lbl, cmb), 0, 5, 2, 1);
        
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
			errorAction.setText("A player has not entered a name");
			return;
		}
		else if (nameOne.equals(nameTwo))
		{
			errorAction.setText("Players must have different names");
			return;
		}
		
		playerOne.setUseAlternateControls(true);
		playerOne.setCarColor(CarColor.CAR_YELLOW.color);
		playerTwo.setCarColor(CarColor.CAR_BLUE.color);
		
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
		if (singlePlayer)
		{
			Image carImage = null;
			if (playerOne.getCarColor().equals(CarColor.CAR_YELLOW.color))
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
		
		sprites.forEach( (sprite) -> {
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
		});
		
		return spritesCopy;
	}

	private void addFinishLine(Image finishLineImage, SpriteHandler sprites)
	{
		FinishLine finishLine = new FinishLine(0, (roadNumberCoefficient+1)*Road.Dimension.HEIGHT.getValue() + Car.Dimension.HEIGHT.getValue(), finishLineImage);
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
			for (Car checkCar : cars)
			{
				if (checkCar.getWinner())
				{
					winnerName = checkCar.getPlayer().getName();
					Label winnerText = new Label( winnerName + " is the winner!");
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
		gc.translate(x + (Car.Dimension.WIDTH.getValue())/2, y + (Car.Dimension.HEIGHT.getValue())/2);
		gc.rotate(car.getRotation());

		gc.drawImage(car.getImage(), -(Car.Dimension.WIDTH.getValue())/2, -(Car.Dimension.HEIGHT.getValue())/2);

		gc.restore();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	private void updatePosition()
	{
		cars.forEach( (car) -> {
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
		});
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
			if (newX > 0 && newX < Car.GAME_CANVAS_WIDTH - Car.Dimension.HEIGHT.getValue())
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
}
