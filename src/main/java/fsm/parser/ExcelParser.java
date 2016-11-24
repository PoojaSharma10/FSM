package fsm.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import fsm.domain.Desk;
import fsm.domain.Floor;
import fsm.domain.UI.FloorObjects;
import fsm.factory.DeskFactory;
import fsm.factory.FloorFactory;
import jxl.Cell;
import jxl.CellType;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelParser {

    @Autowired
    DeskFactory deskFactory;

    @Autowired
    FloorFactory floorFactory;

    private boolean isValidDesk(Cell cell) {
        if (cell.getContents().isEmpty() || cell.getType() != CellType.NUMBER) {
            return false;
        }
        return true;
    }


    public FloorObjects parseFloorDetails(String path) {  // Returs List of Desk and floor information
        int minimumX = 0, minimumY = 0, maximumX, maximumY;
        File workbook = new File(path);
        List<Desk> desks = new ArrayList<Desk>();
        HashSet<String> mergedCellsValues = new HashSet<String>();
        Workbook w = null;
        try {
            w = Workbook.getWorkbook(workbook);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        Sheet sheet = w.getSheet(0);
        Range mergedCells[] = sheet.getMergedCells();
        maximumX = sheet.getColumns() + 1;
        maximumY = sheet.getRows() + 1;
        for (Range range : mergedCells) {
            Cell topLeft = range.getTopLeft();
            Cell bottomRight = range.getBottomRight();
            if (!isValidDesk(topLeft)) {
                continue;
            }
            String deskID = topLeft.getContents();
            int y = topLeft.getRow();
            int x = topLeft.getColumn();
            int width = 1 + bottomRight.getColumn() - topLeft.getColumn();
            int height = 1 + bottomRight.getRow() - topLeft.getRow();
            Desk desk = deskFactory.createDesk(deskID, x, y, width, height);
            mergedCellsValues.add(deskID);
            desks.add(desk);

        }
        boolean foundMinimumFlag = false;
        for (int i = 0; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getColumns(); j++) {
                Cell cell = sheet.getCell(j, i);
                if (!foundMinimumFlag) {
                    if (!cell.getContents().isEmpty()) {
                        minimumX = i;
                        minimumY = j;
                        foundMinimumFlag = true;
                    }
                }
                if (!isValidDesk(cell)) {
                    continue;
                }
                String deskID = cell.getContents();
                if (!mergedCellsValues.contains(deskID)) {
                    int y = cell.getRow();
                    int x = cell.getColumn();
                    int width = 1;
                    int height = 1;
                    Desk desk = deskFactory.createDesk(deskID, x, y, width, height);
                    desks.add(desk);
                }
            }
        }
        Floor floor = floorFactory.create(minimumX, minimumY, maximumX, maximumY);
        return new FloorObjects(desks, floor);
    }
}