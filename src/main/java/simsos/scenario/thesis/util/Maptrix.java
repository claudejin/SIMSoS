package simsos.scenario.thesis.util;

import java.lang.reflect.Array;

public class Maptrix<T> {
    private Class<T> clazz;

    private int xSize;
    private int ySize;

    private T values[][];

    public Maptrix(Class<T> _clazz, int _xSize, int _ySize) {
        this.clazz = _clazz;
        this.xSize = _xSize;
        this.ySize = _ySize;

        values = (T[][]) new Object[xSize][ySize];
        this.reset();
    }

    public void reset() {
        for (int i = 0; i < xSize; i++)
            for (int j = 0; j < ySize; j++)
                try {
                    values[i][j] = (T) this.clazz.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
    }

    public void setValue(int x, int y, T val) {
        if (x >= xSize || y >= ySize || x < 0 || y < 0)
            throw new IndexOutOfBoundsException();

        values[x][y] = val;
    }

    public T getValue(int x, int y) {
        if (x >= xSize || y >= ySize || x < 0 || y < 0)
            throw new IndexOutOfBoundsException();

        return values[x][y];
    }
}
