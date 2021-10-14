import syntaxtree.*;

// transfer java to vapor 
public class J2V {
	public static void main(String[] args) {
		try {
			Goal goal = new MiniJavaParser(System.in).Goal();
			BuildSTVisitor bvisitor = new BuildSTVisitor();
			goal.accept(bvisitor);
			TypeCheckVisitor tcvisitor = new TypeCheckVisitor(bvisitor.getSymbolTable());
			goal.accept(tcvisitor);
			if (!tcvisitor.isAnyError()) {
				TranslateVisitor tvisitor = new TranslateVisitor(bvisitor.getSymbolTable());
				goal.accept(tvisitor);
			}
		} catch (ParseException e) {
			System.out.println(e);
		}		
	}
}
