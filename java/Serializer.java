import java.util.*;

public class Serializer extends HelloBaseVisitor<String>{

    @Override
    public String visitXqvar(HelloParser.XqvarContext ctx) {
        return "$"+ctx.Letter().getText();
    }

    @Override
    public String visitXqstr(HelloParser.XqstrContext ctx) {
        return ctx.StringSentence().getText();
    }

    @Override
    public String visitXqslash(HelloParser.XqslashContext ctx) {
        return visit(ctx.xq())+"/"+visit(ctx.rp());
    }

    @Override
    public String visitXqdoubleslash(HelloParser.XqdoubleslashContext ctx) {
        return visit(ctx.xq())+"//"+visit(ctx.rp());
    }

    @Override
    public String visitXqtag(HelloParser.XqtagContext ctx) {
        return "<"+ctx.Letter(0).getText()+">{"+visit(ctx.xq())+"}</"+ctx.Letter(1).getText()+">";
    }

    @Override
    public String visitXqquote(HelloParser.XqquoteContext ctx) {
        return visit(ctx.xq(0))+","+visit(ctx.xq(1));
    }

    @Override
    public String visitXqreturnclause(HelloParser.XqreturnclauseContext ctx) {
        return "return"+visit(ctx.xq());
    }

    @Override
    public String visitApSingleSlash(HelloParser.ApSingleSlashContext ctx) {
        return "doc(\""+ctx.Letter().getText()+"\")/"+visit(ctx.rp());
    }

    @Override
    public String visitApDoubleSlash(HelloParser.ApDoubleSlashContext ctx) {
        return "doc(\""+ctx.Letter().getText()+"\")//"+visit(ctx.rp());
    }

    @Override
    public String visitRpTagName(HelloParser.RpTagNameContext ctx) {
        return ctx.Letter().getText();
    }

    @Override
    public String visitRpSingleSlash(HelloParser.RpSingleSlashContext ctx) {
        return visit(ctx.rp(0))+"/"+visit(ctx.rp(1));
    }

    @Override
    public String visitRpDoubleSlash(HelloParser.RpDoubleSlashContext ctx) {
        return visit(ctx.rp(0))+"//"+visit(ctx.rp(1));
    }

    @Override
    public String visitRpGetTextNode(HelloParser.RpGetTextNodeContext ctx) {
        return "text()";
    }

    @Override
    public String visitRpCurrent(HelloParser.RpCurrentContext ctx) {
        return ".";
    }

    @Override
    public String visitRpParent(HelloParser.RpParentContext ctx) {
        return "..";
    }

    @Override
    public String visitRpAllChildren(HelloParser.RpAllChildrenContext ctx) {
        return "*";
    }

    @Override
    public String visitRpGetAttribute(HelloParser.RpGetAttributeContext ctx) {
        return "@"+ctx.Letter().getText();
    }

    @Override
    public String visitRpAllAttribute(HelloParser.RpAllAttributeContext ctx) {
        return "@*";
    }

    @Override
    public String visitCondeq(HelloParser.CondeqContext ctx) {
        return visit(ctx.xq(0))+"eq"+visit(ctx.xq(1));
    }
}

