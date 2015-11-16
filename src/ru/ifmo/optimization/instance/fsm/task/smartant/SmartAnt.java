package ru.ifmo.optimization.instance.fsm.task.smartant;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class SmartAnt {
	public Cell[][] field;
	public Cell[][] bkpField;
	private Direction currentDirection;
	private Cell currentCell;
	public int fieldWidth;
	public int fieldHeight;
	public static final int NUMBER_OF_APPLES = 157;

	public SmartAnt(String fieldFile) {
		field = readField(fieldFile);
		currentCell = field[0][0];
		currentDirection = Direction.EAST;
	}
	
	public SmartAnt(SmartAnt other) {
		fieldWidth = other.fieldWidth;
		fieldHeight = other.fieldHeight;
		
		field = new Cell[fieldHeight][fieldWidth];
		for (int i = 0; i < fieldHeight; i++) {
			for (int j = 0; j < fieldWidth; j++) {
				field[i][j] = other.field[i][j].clone();
			}
		}
		currentDirection = other.currentDirection;
		currentCell = other.currentCell.clone();
	}

	public void reset() {
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				field[i][j] = bkpField[i][j].clone();
			}
		}
		
		currentCell = field[0][0];
		currentDirection = Direction.EAST;
	}
	
	private Cell[][] readField(String filename) {
		Cell[][] f = null;		
		try {
			Scanner in = new Scanner(new File(filename));
			fieldWidth = in.nextInt();
			fieldHeight = in.nextInt();

			f = new Cell[fieldHeight][fieldHeight];
			bkpField = new Cell[fieldHeight][fieldHeight];

			for (int i = 0; i < fieldHeight; i++) {
				for (int j = 0; j < fieldWidth; j++) {
					f[i][j] = new Cell(i, j, in.nextInt());
					bkpField[i][j] = f[i][j].clone();
				}
			}
			return f;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File \"" + filename
					+ "\" not found! Program will exit.");
			System.exit(1);
		}
		return f;
	}

	public void turnLeft() {
		currentDirection = currentDirection.left();
	}

	public void turnRight() {
		currentDirection = currentDirection.right();
	}

	public boolean nextIsFood() {	
		return field[getCurrentCell().x][getCurrentCell().y].nextCell(currentDirection).hasFood();
	}

	public int move() {						
		setCurrentCell(currentCell.nextCell(currentDirection));
		if (currentCell.hasFood()) {
			currentCell.eatFood();
			return 1;
		}
		return 0;
	}

	public Direction getDirection() {
		return currentDirection;
	}

	public Cell getCurrentCell() {
		return currentCell;
	}

	public void setCurrentCell(Cell cell) {
		this.currentCell = cell;
	}

	public enum Direction {
		NORTH, SOUTH, WEST, EAST;

		public Direction left() {
			switch (this) {
			case NORTH:
				return WEST;
			case SOUTH:
				return EAST;
			case EAST:
				return NORTH;
			case WEST:
				return SOUTH;
			default:
				return null;
			}
		}

		public Direction right() {
			switch (this) {
			case NORTH:
				return EAST;
			case SOUTH:
				return WEST;
			case EAST:
				return SOUTH;
			case WEST:
				return NORTH;
			default:
				return null;
			}
		}
	}

	public class Cell implements Cloneable {
		public int x;
		public int y;
		private int food;		

		public Cell clone() {
			return new Cell(this);
		}

		public Cell(Cell other) {
			this.x = other.x;
			this.y = other.y;
			this.food = other.food;		
		}

		public Cell(int x, int y, int food) {
			this.x = x;
			this.y = y;
			this.food = food;			
		}
		
		public boolean hasFood() {
			return food > 0; 
		}

		public int eatFood() {			
			food -= 1;
			return 1;			
		}
		
		public Cell nextCell(Direction direction) {
			switch (direction) {
			 case WEST:
                 return field[x][(y + fieldHeight - 1) % fieldHeight];//.clone();
             case EAST:
                 return field[x][(y + 1) % fieldHeight];//.clone();
             case NORTH:
                 return field[(x + fieldWidth - 1) % fieldWidth][y];//.clone();
             case SOUTH:
                 return field[(x + 1) % fieldWidth][y];//.clone();
             default:
            	 return null;
			}
		}
	}
}

