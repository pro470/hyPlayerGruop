# hyPlayerGroup

## Overview

HyPlayerGroup is a content-focused group and permission system for Hytale. HyPlayerGroup enables you to determine which
players belong to a specific group, a function not supported by Hytale’s Permissions System. This capability is
essential for creating content-driven features, such as displaying members in a particular group chat. There are several
additional distinctions from Hytale’s Permissions System, which primarily addresses server administration.

## Integration

HyPlayerGroup completely integrates with Hytale’s Permission system. The class PlayerGroupPermissionsProvider that
implements PermissionProvider is added to the PermissionModule Providers list. So the hasPermission method on
PermissionModule and PlayerRef will also check HyPlayerGroup Permissions.

If you want to add permission to a Group or Player, you can get the provider with `getProvider`. The PermissionModule
does not work at the moment. Hytale didn't add an option for that yet, but we sent all groups and permissions events
from Hytale.

For the creation, disband, and all parent events, we will send our own events because Hytale does not send such events.

## Group System

One feature is that a group can nest within another group, enabling complex social structures in your game. For example,
if you have a guild system or a pirate crew system inspired by One Piece, crews can form alliances or join larger crews
while maintaining their distinct identity. Nested groups also allow you to represent divisions, such as the whitebread
pirates. With permissions, a group automatically inherits all permissions from its “ancestors”.

## Performance/Architecture

How the current Permission system in Hytale works right now is that it has read and write locks. So it can only have one
writer at the time, but if you want the players to create, modify, or disband groups, this is not a good idea. All world
threads that need to read the groups or permission data would have to wait until all changes are done, while changes
don't even affect them.

HyPlayerGroup does it differently. All changes are a request. Those are submitted in a queue. A separate thread consumes
those requests and builds the immutable DAGFlat class, which the HyPlayerGroup PermissionProvider uses through an atomic
reference. HyPlayerGroup never blocks a world thread with that.

But wouldn't I read inconsistent data when a new DAGFlat builds?

Yes, that’s on purpose, because I want the user of this plugin, mod, or however you call it, to decide to wait or not,
because if you have a TickingSystem that runs every tick, it should correct itself in the next tick. There is no need to
block the world thread.

I should also mention that you only block the thread you called the methods. Other world threads just run without blocking. So every thread decides by itself.  If it wants to wait for the changes.

### How to wait

If you need to wait for consistent data, there is a method, `ifNewWaitOnBuildingDAGFlat`.

A better solution is to use `getAffected`, which returns all affected groups and players' UUIDs. This is available
before the build process of the DAGFlat, which, on average, is the longest process.

## API

HyPlayerGroupPlugin:

methods:

```Java

public static HyPlayerGroupPlugin get();

public PlayerGroupPermissionsProvider getProvider();

public PlayerGroupDAGFlat getDagFlat();

public void submitRequest(PlayerGroupGroupChangeRequest request);

public Boolean isBuildingDAGFlat();

public PlayerGroupDAGFlat getDAGFlat();

public PlayerGroupPubAffected getAffected();

public PlayerGroupDAGFlat ifNewWaitOnBuildingDAGFlat();
```

all requests:

* AddGroupParentRequest
* AddGroupPermissonRequest
* AddPlayerPermissionRequest
* AddPlayerToGroupRequest
* CreateGroupRequest
* DependedAndRequest
* DependedOnFailRequest
* DependedOnSuccessRequest
* DependedOrRequest
* DisbandGroupRequest
* RemoveGroupParentRequest
* RemoveGroupPermissionRequest
* RemovePlayerFromGroupRequest
* RemovePlayerPermissonRequest

For the dependent request, the request it depends on has to be submitted before the dependent request.

## Commands

right now work in progress

## Testing

I created a little “simulation Test”, which is just a system that sends random requests to the queue with real and null
UUIDs for groups. The validators are static methods that check the consistency in DAGFlat and between the DAG. I have
unit tests for the Validators, so I know they would throw the Assert error when the data is inconsistent. 

















