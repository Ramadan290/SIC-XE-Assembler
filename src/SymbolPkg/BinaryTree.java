package SymbolPkg;

import Assembler.Utility;

import java.util.ArrayList;



public class BinaryTree{
    private Node rootNode;

    /**
     * Private method to insert the node to the binary tree.
     *
     * @param parentNode Current node. We start with the rootNode as parent Node,
     *                   and if it's null, the new node is the root node.
     * @param newNode New Node we want to insert.
     */
    private void insert(Node parentNode, Node newNode){
        // If the parentNode is null, this is the first node in the tree
        if(parentNode == null){
            rootNode = newNode;
            return;
        }

        // if the parentNode isn't null
        if(newNode.getKey().compareTo(parentNode.getKey()) < 0){
            if(parentNode.leftNode != null){
                insert(parentNode.leftNode, newNode);
            } else {
                parentNode.leftNode = newNode;
            }
        }
        else if(newNode.getKey().compareTo(parentNode.getKey()) > 0){
            if(parentNode.rightNode != null){
                insert(parentNode.rightNode, newNode);
            } else {
                parentNode.rightNode = newNode;
            }
        }
    }

    /**
     * Insert a new node in the binary tree.
     *
     * @param newNode New node we want to insert.
     */
    public void insert(Node newNode){
        // insert starts with comparing with rootNode
        insert(this.rootNode, newNode);
    }

    /**
     * Private search method of binary tree. This method is called by the public one argument search method.
     */

    private Node search(Node parentNode, Node node){
        if(parentNode == null){
            return null;
        }
        else if(node.getKey().equals(parentNode.getKey())){
            return parentNode;
        }
        else if(node.getKey().compareTo(parentNode.getKey()) < 0){
            return search(parentNode.leftNode, node);
        }
        else if(node.getKey().compareTo(parentNode.getKey()) > 0){
            return search(parentNode.rightNode, node);
        }

        // placeholder, code execution shouldn't come to this point.
        return null;
    }

    /**
     * Searches for a Node is the Binary Tree
     *
     * @param node search for this node
     * @return returns the node if found
     */

    public Node search(Node node){
        // search starts with comparing with rootNode
        return search(this.rootNode, node);
    }

    /**
     * This protected view method is the actual recursive method that travels the binary tree.
     *
     */
    private void view(Node node){
        if(node.leftNode != null){
            view(node.leftNode);
        }

        System.out.println(node);

        if(node.rightNode != null){
            view(node.rightNode);
        }
    }

    /**
     *  Prints the BinaryTree on the console
     *
     *
     */
    public void view(){
        if(rootNode != null){
            view(rootNode);
        }
    }

    public ArrayList<Node> getAll(){
        ArrayList<Node> nodeList = new ArrayList<>();

        if(rootNode != null){
            getAll(rootNode, nodeList);
        }

        return nodeList;
    }

    private void getAll(Node node, ArrayList<Node> nodeList){
        if(node.leftNode != null){
            getAll(node.leftNode, nodeList);
        }

        nodeList.add(node);

        if(node.rightNode != null){
            getAll(node.rightNode, nodeList);
        }
    }

}