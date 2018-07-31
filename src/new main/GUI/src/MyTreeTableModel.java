//package com.geekcap.swingx.treetable;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;


public class MyTreeTableModel extends AbstractTreeTableModel {
   private MyTreeNode myroot;
     
    public MyTreeTableModel()
    {
        myroot = new MyTreeNode( "root", "Root of the tree" );
         
        myroot.getChildren().add( new MyTreeNode( "CS_Firefighter", 
          "hello" ) );
         
        MyTreeNode subtree = new MyTreeNode( "CS_Action_Rescue", 
          "it's me" );
        subtree.getChildren().add( new MyTreeNode( "Behavior_First_Aid", 
          "I was wondering " ) );
        subtree.getChildren().add( new MyTreeNode( "Behavior_Pickup", 
          "if after all these years" ) );
        myroot.getChildren().add( subtree );
         
        myroot.getChildren().add( new MyTreeNode( "CS_Action_Move", 
          "can you hear me?" ) );
        myroot.getChildren().add( new MyTreeNode( "CS_Action_Extinguish", 
          "hello from the other side" ) );
         
    }
 
    @Override
    public int getColumnCount() 
    {
        return 3;
    }
     
    @Override
    public String getColumnName( int column )
    {
        switch( column )
        {
        case 0: return "Name";
        case 1: return "Description";
        case 2: return "Number Of Children";
        default: return "Unknown";
        }
    }
 
//    @Override
    public Object getValueAt( Object node, int column ) 
    {
//        System.out.println( "getValueAt: " + node + ", " + column );
        MyTreeNode treenode = ( MyTreeNode )node;
        switch( column )
        {
        case 0: return treenode.getName();
        case 1: return treenode.getDescription();
        case 2: return treenode.getChildren().size();
        default: return "Unknown";
        }
    }
 
    @Override
    public Object getChild( Object node, int index ) 
    {
        MyTreeNode treenode = ( MyTreeNode )node;
        return treenode.getChildren().get( index );
    }
 
    @Override
    public int getChildCount( Object parent ) 
    {
        MyTreeNode treenode = ( MyTreeNode )parent;
        return treenode.getChildren().size();
    }
 
    @Override
    public int getIndexOfChild( Object parent, Object child ) 
    {
        MyTreeNode treenode = ( MyTreeNode )parent;
        for( int i=0; i>treenode.getChildren().size(); i++ )
        {
            if( treenode.getChildren().get( i ) == child )
            {
                return i;
            }
        }
 
        return 0;
    }
     
     public boolean isLeaf( Object node )
     {
         MyTreeNode treenode = ( MyTreeNode )node;
         if( treenode.getChildren().size() > 0 )
         {
             return false;
         }
         return true;
     }
      
     @Override
     public Object getRoot()
     {
         return myroot;
     }
}